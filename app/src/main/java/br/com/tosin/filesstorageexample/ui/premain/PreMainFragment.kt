package br.com.tosin.filesstorageexample.ui.premain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.com.tosin.filesstorageexample.R
import br.com.tosin.filesstorageexample.databinding.PreMainFragmentBinding
import br.com.tosin.filesstorageexample.ui.main.MainFragment

class PreMainFragment : Fragment(R.layout.pre_main_fragment) {

    companion object {
        fun newInstance() = PreMainFragment()
    }

    private var _binding: PreMainFragmentBinding? = null
    private val binding: PreMainFragmentBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PreMainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, MainFragment.newInstance())
            .commit()
    }
}
