package com.youbook.glow.ui.fragment.favourite_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.youbook.glow.databinding.FragmentFavouriteBinding
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.base.BaseFragment
import com.android.fade.ui.fragment.favourite_list.FavouriteRepository
import com.android.fade.ui.fragment.favourite_list.FavouriteViewModel
import com.android.fade.ui.fragment.favourite_list.MyFavBarberAdapter
import com.android.fade.ui.fragment.favourite_list.ResultItem
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.snackbar
import kotlinx.coroutines.launch
import java.util.HashMap

class FavouriteFragment : BaseFragment<FavouriteViewModel, FragmentFavouriteBinding, FavouriteRepository>() {

    private var user_id: String? = ""
    private lateinit var favBarberAdapter: MyFavBarberAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpServicesRecyclerView()
        getFavouriteBarber()

        viewModel.getFavouriteBarber.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.success!!) {

                        if (it.value.result!!.isEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                            favBarberAdapter.updateList(it.value.result as List<ResultItem>)
                        } else {
                            favBarberAdapter.updateList(it.value.result as List<ResultItem>)
                            binding.tvNoData.visibility = View.GONE
                        }
                    }

                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                }
            }
        }

        viewModel.makeBarberFavUnFav.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.root.snackbar(it.value.message!!)
                    getFavouriteBarber()
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                }
            }
        }
    }

    private fun setUpServicesRecyclerView() {
        binding.favRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favBarberAdapter = MyFavBarberAdapter(requireContext()){ item, po ->
            makeBarberFavUnFav(item)
        }
        binding.favRecyclerView.adapter = favBarberAdapter
    }

    private fun makeBarberFavUnFav(item: ResultItem) {
        user_id = Prefrences.getPreferences(requireContext(), Constants.USER_ID)
        val barberId = item.barber_id
        val params = HashMap<String, String>()
        params["user_id"] = user_id!!
        params["barber_id"] = barberId.toString()

            // if already fav make it un-favourite
         params["type"] = "0"


        viewModel.viewModelScope.launch {
            viewModel.makeBarberFavUnFav(params)
        }
    }

    private fun getFavouriteBarber() {
        viewModel.viewModelScope.launch {
            viewModel.getFavouriteBarber()
        }
    }

    override fun getViewModel() = FavouriteViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFavouriteBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = FavouriteRepository(
        MyApi.getInstanceToken(
            Prefrences.getPreferences(requireContext(), Constants.API_TOKEN)!!
        )
    )

}