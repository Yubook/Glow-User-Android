package com.android.fade.ui.appointment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.youbook.glow.R
import com.android.fade.ServicesItem
import com.youbook.glow.calendar.DateSelectorDecorator
import com.android.fade.extension.loadingImage
import com.android.fade.extension.visible
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.barber_details.AvailableSlotsItem
import com.android.fade.ui.payment_details.PaymentDetailsActivity
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.snackbar
import com.youbook.glow.databinding.ActivityAppointmentsBinding
import com.youbook.glow.ui.appointment.AppointmentViewModelFactory
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.activity_appointments.*
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AppointmentsActivity : AppCompatActivity(), View.OnClickListener, OnDateSelectedListener {
    private lateinit var binding: ActivityAppointmentsBinding
    private lateinit var viewModel: AppointmentViewModel
    private lateinit var adapter: DriverSlotsAdapter
    private var driverId: String? = null
    private var driverName: String? = null
    private var driverRating: String? = null
    private var driverTotalReview: String? = null
    private var driverImage: String? = null
    private var selectedDate: String? = null
    private var driverServices: String? = null
    private var orderLat: String? = null
    private var orderLon: String? = null
    private var isReschedule: String? = null
    private var oldSlotId: String? = null
    private var selectedServices = ArrayList<ServicesItem>()
    private var totalTime = 0
    private var selectedServicesName = ""
    private var selectedServicesIds = ArrayList<String>()
    private var selectedSlotsIds = ArrayList<String>()
    private var requiredNumberOfSlots = 0
    private var needSlotsToSelect = 0
    private var selectedServicesPrice = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            AppointmentViewModelFactory(
                AppointmentsRepository(
                    MyApi.getInstanceToken(
                        Prefrences.getPreferences(
                            this,
                            Constants.API_TOKEN
                        )!!
                    )
                )
            )
        ).get(AppointmentViewModel::class.java)


        setDriverData()
        setClickListener()
        setUpCalendar()

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(CalendarDay.today().date))
            .commit()

        // Select Today's date Default
        binding.calendarView.setDateSelected(CalendarDay.from(CalendarDay.today().date), true)

        selectedDate = CalendarDay.today().date.toString()
        getDriverSlots(selectedDate!!)


        viewModel.driverSlotsResponse.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    if (it.value.result != null) {
                        if (it.value.result.availableSlots!!.isEmpty()) {
                            binding.tvSlots.text = "No Slots Available"
                        } else {
                            binding.tvSlots.text = "Available Slots"
                        }

                        requiredNumberOfSlots = it.value.result.slotsRequired!!
                        needSlotsToSelect = requiredNumberOfSlots
                        binding.tvSlotsToSelect.text =
                            "Slots need to select : $requiredNumberOfSlots"

                        setUpSlotAdapter()
                        adapter.updateList(it.value.result.availableSlots.filter { it!!.isBooked == 0 } as List<AvailableSlotsItem>)
                    } else {
                        binding.root.snackbar(it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    getDriverSlots(selectedDate!!)
                }
            }
        }
    }

    private fun setUpSlotAdapter() {
        binding.slotRecyclerView.layoutManager = GridLayoutManager(this, 5)
        adapter = DriverSlotsAdapter(this, requiredNumberOfSlots) {
            needSlotsToSelect = requiredNumberOfSlots - adapter.getSelectedSlots().size
            "Slots need to select : $needSlotsToSelect".also { binding.tvSlotsToSelect.text = it }
        }
        binding.slotRecyclerView.adapter = adapter
    }

    private fun getDriverSlots(date: String) {
        viewModel.viewModelScope.launch {
            viewModel.getDriverSlots(driverId, date, "$totalTime")
        }
    }

    fun formatDate(fromFormat: String?, toFormat: String?, dateToFormat: String?): String? {
        val inFormat = SimpleDateFormat(fromFormat)
        var date: Date? = null
        try {
            date = inFormat.parse(dateToFormat)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val outFormat = SimpleDateFormat(toFormat)
        return outFormat.format(date)
    }

    private fun setDriverData() {
        driverId = intent.getStringExtra(Constants.DRIVER_ID)
        driverName = intent.getStringExtra(Constants.DRIVER_NAME)
        driverImage = intent.getStringExtra(Constants.DRIVER_IMAGE)
        driverRating = intent.getStringExtra(Constants.DRIVER_RATING)
        driverTotalReview = intent.getStringExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT)
        driverServices = intent.getStringExtra(Constants.DRIVER_SERVICE)
        selectedServices = intent.getParcelableArrayListExtra(Constants.BOOKED_SERVICE)!!
        loadingImage(this, Constants.STORAGE_URL + driverImage!!, binding.ivDriverImage, true)
        binding.tvDriverName.text = driverName

        for ((i, item) in selectedServices.withIndex()) {
            totalTime += item.service!!.time!!.toInt()
            selectedServicesPrice += item.service_price!!
            if(i == 0){
                selectedServicesName += item.service!!.name.toString()
            } else {
                selectedServicesName += ", "
                selectedServicesName += item.service!!.name.toString()
            }
            selectedServicesIds.add(item.service!!.id.toString())

        }
    }

    private fun setUpCalendar() {
        binding.calendarView.setOnDateChangedListener(this)
    }

    private fun setClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.tvBookAppointment.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.tvBookAppointment -> bookAppointment()
        }
    }

    private fun bookAppointment() {
        if (adapter.getSelectedSlots().size > 0) {
            if (adapter.getSelectedSlots().size < requiredNumberOfSlots) {
                if (needSlotsToSelect == 1) {
                    binding.root.snackbar("You need to select $needSlotsToSelect more slot")
                } else {
                    binding.root.snackbar("You need to select $needSlotsToSelect more slots")
                }
            } else {
                goToPaymentActivity()
            }
        } else {
            binding.root.snackbar(getString(R.string.empty_slots))
        }

    }

    private fun goToPaymentActivity() {

        for (i in adapter.getSelectedSlots()){
            selectedSlotsIds.add(i.id.toString())
        }
        val intent = Intent(this, PaymentDetailsActivity::class.java)
        intent.putExtra(Constants.DRIVER_ID, driverId)
        intent.putExtra(Constants.DRIVER_NAME, driverName)
        intent.putExtra(Constants.DRIVER_IMAGE, driverImage)
        intent.putExtra(Constants.DRIVER_RATING, driverRating)
        intent.putExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT, driverTotalReview)
        intent.putExtra(Constants.BOOKED_SERVICE, selectedServicesName)
        intent.putStringArrayListExtra(Constants.BOOKED_SERVICE_ID, selectedServicesIds)
        intent.putStringArrayListExtra(Constants.ORDER_SLOT_ID, selectedSlotsIds)
        intent.putExtra(Constants.DRIVER_SERVICE_PRICE, selectedServicesPrice.toString())
        val slotTime =
            formatDate("hh:mm", "hh:mm a", adapter.getSelectedSlots()[0].time!!.time.toString())
        intent.putExtra(
            Constants.BOOKED_DATE, formatDate(
                "yyyy-MM-dd", "EEEE, dd MMM yyyy",
                selectedDate
            ).plus(" @ ").plus(slotTime)
        )
        startActivity(intent)
    }

    /*private fun goToSelectServiceActivity() {
        val intent = Intent(this, SelectServiceActivity::class.java)
        intent.putExtra(Constants.DRIVER_ID, driverId)
        intent.putExtra(Constants.DRIVER_NAME, driverName)
        intent.putExtra(Constants.DRIVER_IMAGE, driverImage)``
        intent.putExtra(Constants.DRIVER_RATING, driverRating)
        intent.putExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT, driverTotalReview)
        intent.putExtra(Constants.DRIVER_SERVICE, driverServices)
        intent.putExtra(Constants.BOOKED_SERVICE, driverServices)
        intent.putExtra(Constants.ORDER_SLOT_ID, adapter.getSelectedSlotId().toString())
        intent.putExtra(Constants.ORDER_LAT, orderLat)
        intent.putExtra(Constants.ORDER_LON, orderLon)
        val bookedSlotTime = adapter.getSelectedSlotName()
        val slotTime = formatDate("hh:mm", "hh:mm a", bookedSlotTime.substring(0, 5))
        intent.putExtra(
            Constants.BOOKED_DATE, formatDate(
                "yyyy-MM-dd", "EEEE, dd MMM yyyy",
                selectedDate
            ).plus(" @ ").plus(slotTime)
        )
        startActivity(intent)
    }*/

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        selectedDate = date.date.toString()
        getDriverSlots(selectedDate!!)
        removeEvent(date)
    }

    private fun removeEvent(day: CalendarDay) {
        binding.calendarView.removeDecorator(DateSelectorDecorator(this, day))
        binding.calendarView.invalidateDecorators()
    }
}