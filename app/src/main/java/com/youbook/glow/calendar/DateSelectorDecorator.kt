package com.youbook.glow.calendar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade


class DateSelectorDecorator(context: Activity, calendarDay: CalendarDay) : DayViewDecorator {
    @SuppressLint("UseCompatLoadingForDrawables")
    var currentDay: CalendarDay? = CalendarDay.from(calendarDay.date)

    private val drawable: Drawable = getDrawable("drawable_black_rounded_corner_bg", context)
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == currentDay
    }


    override fun decorate(view: DayViewFacade) {
        view.setSelectionDrawable(drawable)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawable(name: String, context: Context) : Drawable{
        val resources: Resources = context.resources
        val resourceId: Int = resources.getIdentifier(
            name, "drawable",
            context.packageName
        )
        return resources.getDrawable(resourceId)
    }

}