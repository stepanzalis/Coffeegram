package ru.beryukhov.coffeegram.view

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.threeten.bp.DayOfWeek
import org.threeten.bp.YearMonth
import org.threeten.bp.format.TextStyle
import ru.beryukhov.coffeegram.R
import ru.beryukhov.coffeegram.app_ui.CoffeegramTheme
import ru.beryukhov.coffeegram.model.NavigationIntent
import ru.beryukhov.coffeegram.model.NavigationStore
import ru.beryukhov.coffeegram.times
import java.text.DateFormatSymbols
import java.util.*

data class DayItem(
    val day: String,
    @DrawableRes val iconId: Int? = null,
    val dayOfMonth: Int? = null
)

@Composable
fun DayCell(
    dayItem: DayItem,
    modifier: Modifier = Modifier,
    navigationStore: NavigationStore
) {
    Column(horizontalGravity = Alignment.CenterHorizontally, modifier =
    if (dayItem.dayOfMonth == null) modifier else
        modifier.clickable(onClick = {
            navigationStore.newIntent(
                NavigationIntent.OpenCoffeeListPage(
                    dayItem.dayOfMonth
                )
            )
        })
    ) {
        with(dayItem) {
            if (iconId != null) {
                Image(
                    vectorResource(id = iconId),
                    modifier = Modifier
                        .preferredSize(32.dp)
                        .fillMaxWidth()
                        //.gravity(Alignment.CenterVertically)
                        .gravity(Alignment.CenterHorizontally)
                )
            } else {
                Icon(
                    Icons.Default.Delete,
                    tint = Color.Transparent,
                    modifier = Modifier.preferredSize(32.dp)
                )
            }

            Text(
                AnnotatedString(
                    text = day,
                    paragraphStyle = ParagraphStyle(textAlign = TextAlign.Center)
                )
            )
        }
    }
}

@Composable
fun WeekRow(dayItems: List<DayItem?>, navigationStore: NavigationStore) {
    val weekDaysItems = dayItems.toMutableList()
    weekDaysItems.addAll(listOf(DayItem("")) * (7 - weekDaysItems.size))
    Column(horizontalGravity = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayItem in weekDaysItems) {
                DayCell(
                    dayItem = dayItem
                        ?: DayItem(""),
                    navigationStore = navigationStore,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Divider()
    }

}

@Composable
fun MonthTableAdjusted(
    weekItems: List<List<DayItem?>>,
    navigationStore: NavigationStore,
    modifier: Modifier = Modifier
) {
    Column(horizontalGravity = Alignment.CenterHorizontally, modifier = modifier) {
        weekItems.map { WeekRow(dayItems = it, navigationStore = navigationStore) }
    }
}

data class WeekDayVectorPair(
    val day: Int,
    val weekDay: DayOfWeek,
    @DrawableRes var iconId: Int? = null
) {
    fun toDayItem(): DayItem =
        DayItem("$day", iconId, day)
}

@Composable
fun MonthTable(
    yearMonth: YearMonth,
    filledDayItemsMap: Map<Int, Int?>,
    navigationStore: NavigationStore,
    modifier: Modifier = Modifier
) {
    val weekDays: List<DayItem> = getWeekDaysNames(
        ContextAmbient.current
    ).map { DayItem(it) }
    val days1to31 = mutableListOf<Int>()
    for (i in 1 until 31) {
        days1to31.add(i)
    }
    val days = days1to31.filter { yearMonth.isValidDay(it) }
        .associateBy<Int, Int, WeekDayVectorPair>(
            { it },
            {
                WeekDayVectorPair(
                    it,
                    yearMonth.atDay(it).dayOfWeek
                )
            })
        .toMutableMap()
    filledDayItemsMap.forEach { days[it.key]?.iconId = it.value }
    val weekDaysStrings =
        getWeekDaysNames(ContextAmbient.current)
    val numberOfFirstDay = weekDaysStrings.indexOf(
        days[1]!!.weekDay.getDisplayName(
            TextStyle.SHORT,
            ContextAmbient.current.resources.configuration.locale
        )
    )
    val daysList: List<WeekDayVectorPair> = days.toList().sortedBy { it.first }.map { it.second }
    val firstWeek: List<DayItem> =
        listOf(DayItem("")) * (numberOfFirstDay) + daysList.take(7 - numberOfFirstDay)
            .map(WeekDayVectorPair::toDayItem)
    val secondToSixWeeks: List<List<DayItem>> = listOf(2, 3, 4, 5, 6).map {
        daysList.drop(7 * (it - 1) - numberOfFirstDay).take(7)
    }.filterNot { it.isEmpty() }
        .map { it.map(WeekDayVectorPair::toDayItem) }

    val weekItems = mutableListOf(
        weekDays,
        firstWeek
    )
    weekItems.addAll(secondToSixWeeks)
    return MonthTableAdjusted(
        weekItems,
        navigationStore,
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun TablePreview() {
    CoffeegramTheme {
        /*Row {
            DayCell(DayItem("Пн"))
            DayCell(DayItem("1"))
            DayCell(DayItem("2", Icons.Default.Call))
        }*/
        /*WeekRow(
            listOf(
                null,
                DayItem("Пн"),
                DayItem("1"),
                DayItem("2", Icons.Default.Call)
            )
        )*/
        SampleTable()
        //Text(getWeekDaysNames(ContextAmbient.current).toString())
    }
}

@Composable
fun SampleTable(modifier: Modifier = Modifier) =
    MonthTable(
        YearMonth.of(2020, 7),
        mapOf(2 to R.drawable.coffee),
        modifier = modifier,
        navigationStore = NavigationStore()
    )


fun getWeekDaysNames(context: Context): List<String> =
    getWeekDaysNames(context.resources.configuration.locale)

fun getWeekDaysNames(locale: Locale): List<String> {
    val list = DateFormatSymbols(locale).shortWeekdays.toMutableList()
    // this fun adds empty string at the beginning
    list.removeAt(0)
    return list
}

fun getEmptyWeek(start: Int, end: Int): List<DayItem> {
    val list = mutableListOf<DayItem>()
    for (i in start until end + 1) {
        list.add(DayItem("$i"))
    }
    return list
}