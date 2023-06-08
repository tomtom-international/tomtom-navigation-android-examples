package com.tomtom.sdk.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.tomtom.sdk.examples.databinding.FragmentRouteProcessBinding
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place

// the fragment initialization parameter ARG_ADDRESS
private const val ARG_ADDRESS = "address"

/**
 * A simple [Fragment] subclass.
 * Use the [RouteProcessFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RouteProcessFragment(place: Place, navigationInterface: NavigateOptionsInterface) :
    Fragment() {
    private var address: String? = null
    private lateinit var binding: FragmentRouteProcessBinding
    private var listener: NavigateOptionsInterface = navigationInterface
    private var destination: Place = place

    interface NavigateOptionsInterface {
        fun onNavigate(destination: GeoPoint)
        fun onCancel()
        fun onRoute(destination: GeoPoint)
        fun removeRoute()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            address = it.getString(ARG_ADDRESS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRouteProcessBinding.inflate(layoutInflater)

        binding.address.text = address

        binding.navigateButton.setOnClickListener {
            listener.onNavigate(destination.coordinate)
        }

        binding.routeButton.setOnClickListener {
            binding.navigateButton.isVisible = true
            binding.routeButton.isVisible = false
            listener.onRoute(destination.coordinate)
        }

        binding.cancelButton.setOnClickListener {
            if (binding.navigateButton.isVisible) {
                binding.navigateButton.isVisible = false
                binding.routeButton.isVisible = true
                listener.removeRoute()
            } else {
                listener.onCancel()
            }
        }

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(place: Place, listener: NavigateOptionsInterface) =
            RouteProcessFragment(place, listener).apply {
                arguments = Bundle().apply {
                    putString(ARG_ADDRESS, place.address?.streetNameAndNumber)
                }
            }
    }
}