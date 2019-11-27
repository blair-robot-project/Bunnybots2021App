package team449.frc.scoutingappbase.fragment

import android.os.Bundle
import android.view.View
import android.widget.Spinner
import team449.frc.scoutingappbase.R
import team449.frc.scoutingappbase.databinding.*
import team449.frc.scoutingappbase.fragment.baseFragment.VMBaseFragment
import team449.frc.scoutingappbase.main.MainActivity

class PrematchFragment: VMBaseFragment<FragmentPrematchBinding>() {
    override val layoutId: Int = R.layout.fragment_prematch
    override fun onResume() {
        super.onResume()
        // Spinners don't update and end up overriding the viewmodel with the old selection
        activity?.findViewById<Spinner>(R.id.team)?.setSelection(Conversions.unbox(viewModel.teamId.value))
        activity?.findViewById<Spinner>(R.id.match)?.setSelection(Conversions.unbox(viewModel.matchId.value))
    }
}

class AutoFragment: VMBaseFragment<FragmentAutoBinding>() {
    override val layoutId: Int = R.layout.fragment_auto
}

class TeleopFragment: VMBaseFragment<FragmentTeleopBinding>() {
    override val layoutId: Int = R.layout.fragment_teleop
}

class EndgameFragment: VMBaseFragment<FragmentEndgameBinding>() {
    override val layoutId: Int = R.layout.fragment_endgame
}

interface SubmitHandler { fun submit(view: View) }

class GeneralFragment: VMBaseFragment<FragmentGeneralBinding>(), SubmitHandler {
    override val layoutId: Int = R.layout.fragment_general

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.handler = this
    }

    override fun submit(view: View) { (activity as MainActivity).submitButtonPressed() }
}