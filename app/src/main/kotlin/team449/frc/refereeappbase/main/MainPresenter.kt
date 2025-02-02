package team449.frc.refereeappbase.main

import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation.findNavController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import team449.frc.refereeappbase.R
import team449.frc.refereeappbase.helpers.*
import team449.frc.refereeappbase.managers.BluetoothManager
import team449.frc.refereeappbase.managers.DataManager
import team449.frc.refereeappbase.model.*


interface Editor {
    fun edit(id: String)
}

class MainPresenter(private val activity: MainActivity) : Editor {

    fun globalHelp() {
        help(R.string.help_global)
    }

    fun bluetooth() {
        GlobalScope.launch {
            activity.preferences?.getString("master", null)?.let { BluetoothManager.connect(it) }
        }
    }

    fun edit() {
        editDialog(activity, DataManager.matchNames, this)
    }

    override fun edit(id: String) {
        val mVM = activity.matchViewModel
        DataManager.retrieveMatch(id)?.let { shadow ->
            DataManager.stashCurrent(MatchShadow(mVM))
            mVM.load(shadow, true)
        }
    }

    fun sync() {
        GlobalScope.launch {
            val written = BluetoothManager.write(makeSyncRequest())
            if (!written) {
                activity.preferences?.getString("master", null)?.let {
                    BluetoothManager.connect(it)
                    BluetoothManager.write(makeSyncRequest())
                }
            }
        }
    }

    fun clearData() {
        confirmationDialog(
            activity,
            activity.getString(R.string.clear_data_title),
            activity.getString(R.string.clear_data_body),
            activity.getString(R.string.clear_data_button)
        )
        { _, _ -> AsyncTask.execute { DataManager.clear() } }
    }

    fun clearEventData() {
        confirmationDialog(
            activity,
            activity.getString(R.string.clear_event_data_title),
            activity.getString(R.string.clear_event_data_body),
            activity.getString(R.string.clear_event_data_button)
        )
        { _, _ ->
            AsyncTask.execute {
                Log.i("-------", "clearing")
                EventData.resetEventData()
                activity.matchViewModel.matchId.postValue(0)
                activity.matchViewModel.teamId.postValue(0)
            }
            clearFile(teamsFile)
            clearFile(matchScheduleFile)
        }
    }

    fun settings() {
        findNavController(activity, R.id.navhost).let { navController ->
            if (navController.currentDestination?.id != R.id.settingsFragment) {
                hideSoftKeyboard(activity)
                navController.navigate(R.id.action_mainContainerFragment_to_settingsFragment)
            }
        }
    }

    fun submit() {
        submitMatch(activity, activity.matchViewModel, { postSubmit() }, activity.pageChanger)
    }

    private fun postSubmit() {
        DataManager.recoverMatch()?.let { activity.matchViewModel.load(it, false) }
            ?: activity.matchViewModel.reset()
        activity.moveToPrematch()
    }

    fun help(messageId: Int) {
        info(activity, activity.getString(R.string.help_title), activity.getString(messageId))
    }

    fun matchChanged() {
        activity.matchViewModel.matchId.value?.let { matchId ->
            teamIdForMatchId(matchId)?.let { teamId ->
                activity.matchViewModel.teamId.value = teamId
            }
        }
        activity.fixSpinners()
    }

    private fun teamIdForMatchId(matchId: Int): Int? {
        EventData.matchSchedule?.let { schedule ->
            schedule[EventData.matches.value?.get(matchId)]?.let { alliances ->
                activity.preferences?.let { prefs ->
                    prefs.getString("driver_station", null)?.toInt()?.let { station ->
                        EventData.teams.value?.indexOf(
                            alliances[prefs.getString("alliance", null) ?: ""]?.get(station)
                        )?.let { teamId ->
                            if (teamId >= 0) return teamId
                        }
                    }
                }
            }
        }
        return null
    }

    val teamSpinnerEnabled: Boolean
        get() = !(activity.preferences?.getBoolean("lockTeamSpinner", false) ?: false &&
                teamIdForMatchId(
                    activity.matchViewModel.matchId.value ?: 0
                ) == activity.matchViewModel.teamId.value ?: -1)

    val preferencesChanged =
        SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
            when (key) {
                "hideNav" -> activity.updateNavBarVisibility()
                "alliance" -> preferences?.getString("alliance", null)?.let {
                    StaticResources.defaultAlliance =
                        if (it == "red") 0 else if (it == "blue") 1 else -1
                    activity.matchViewModel.alliance.value = StaticResources.defaultAlliance
                    matchChanged()
                }
                "driver_station" -> matchChanged()
                "lockTeamSpinner" -> if (preferences.getBoolean(key, false)) {
                    matchChanged()
                }
            }
        }

    /**
     * Increment or decrement a value depending on the view
     */
    fun incrementDecrementValue(view: View) {
        val vm = activity.matchViewModel
        when (view.id) {
            R.id.zone1Inc -> inc(vm.zone1Crates)
            R.id.zone1Dec -> dec(vm.zone1Crates)
            R.id.zone2Inc -> inc(vm.zone2Crates)
            R.id.zone2Dec -> dec(vm.zone2Crates)
            R.id.zone3Inc -> inc(vm.zone3Crates)
            R.id.zone3Dec -> dec(vm.zone3Crates)
            R.id.zone4Inc -> inc(vm.zone4Crates)
            R.id.zone4Dec -> dec(vm.zone4Crates)
        }
        checkForExcessCrates()
    }

    fun checkForExcessCrates() {
        val vm = activity.matchViewModel
        val totalCrates =
            (vm.zone1Crates.value ?: 0) + (vm.zone2Crates.value ?: 0) + (vm.zone3Crates.value
                ?: 0) + (vm.zone4Crates.value ?: 0)
        if (totalCrates > NUM_CRATES) {
            AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AlertDialogCustom))
                .setTitle("Too many crates")
                .setMessage("You can only have $NUM_CRATES crates, but your selections say the robot has stacked $totalCrates crates.")
                .setNeutralButton("Ok"){_, _ -> }
                .show()
                .findViewById<TextView>(android.R.id.message)?.textSize = StaticResources.dialogTextSize
        }
    }

    private fun inc(mld: MutableLiveData<Int>) = update(mld) { Math.min(it + 1, NUM_CRATES) }

    private fun dec(mld: MutableLiveData<Int>) = update(mld) { Math.max(0, it - 1) }

    private fun <T> update(mld: MutableLiveData<T>, fn: (T) -> T) {
        mld.value = mld.value?.let(fn)
    }

    fun setBunnyZone(bunny: View) = toggleBunny(bunny, activity.matchViewModel)

    fun onWindowFocusChange() {
        activity.updateNavBarVisibility()
    }

    fun onBackPressed() {
        activity.onSupportNavigateUp()
    }
}