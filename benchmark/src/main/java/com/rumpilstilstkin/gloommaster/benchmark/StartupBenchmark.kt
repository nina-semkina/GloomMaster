package com.rumpilstilstkin.gloommaster.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rumpilstilstkin.gloommaster.testtags.screens.start.StartScreenTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /** Cold start after clearing app data: the measured launch performs the initial database fill. */
    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun startupWithFirstDatabaseFill() = benchmarkRule.measureRepeated(
        packageName = TestConsts.TARGET_PACKAGE,
        metrics = listOf(
            StartupTimingMetric(),
            TraceSectionMetric("DatabaseFiller.fillDatabase"),
        ),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        iterations = 15,
        startupMode = StartupMode.COLD,
        setupBlock = {
            pressHome()
            device.executeShellCommand("pm clear $packageName")
        },
    ) {
        pressHome()
        startActivityAndWait()
        waitForTag(StartScreenTestTags.ROOT)
    }

    /** Cold start after one unmeasured launch has initialized the database. */
    @Test
    fun startupWithInitializedDatabase() = benchmarkRule.measureRepeated(
        packageName = TestConsts.TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        iterations = 15,
        startupMode = StartupMode.COLD,
        setupBlock = {
            pressHome()
            device.executeShellCommand("pm clear $packageName")
            startActivityAndWait()
            waitForTag(StartScreenTestTags.ROOT)
            pressHome()
        },
    ) {
        pressHome()
        startActivityAndWait()
        waitForTag(StartScreenTestTags.ROOT)
    }
}
