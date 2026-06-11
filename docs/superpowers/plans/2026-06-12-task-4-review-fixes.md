# Fix Issues found in Code Review for Task 4 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix critical balance display bug, implement duration stepper, and correct chip labels/logic in HomeFragment.

**Architecture:** 
- `TerminalFragment`: Observe `TradingViewModel.virtualBalance` for the top balance display. Implement logic to cycle through fixed duration values for the stepper buttons.
- `HomeFragment`: Update chip labels in layout and ensure logic matches "Trending", "Gainers", and "Losers" filters.

**Tech Stack:** Java, Android SDK, View Binding, ViewModel.

---

### Task 1: Fix Balance Display in TerminalFragment

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java`

- [ ] **Step 1: Stop observing live price for tvBalance and observe virtualBalance instead**

```java
<<<<
        cryptoVm.livePrice.observe(getViewLifecycleOwner(), price -> {
            currentPrice = price;
            b.tvBalance.setText(CurrencyFormatter.formatUsd(price));
        });
====
        cryptoVm.livePrice.observe(getViewLifecycleOwner(), price -> {
            currentPrice = price;
        });

        tradingVm.virtualBalance.observe(getViewLifecycleOwner(), bal ->
            b.tvBalance.setText(CurrencyFormatter.formatBalance(bal)));
>>>>
```

- [ ] **Step 2: Initialize balance from prefs if needed (optional but good for UX)**
Actually, observing `virtualBalance` is enough as it should emit the initial value.

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java
git commit -m "fix(terminal): display account balance instead of coin price in tv_balance"
```

### Task 2: Implement Duration Stepper in TerminalFragment

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java`

- [ ] **Step 1: Define fixed durations and index**

```java
<<<<
    private double stakeAmount = 10.0;
    private int durationSeconds = 60;
    private String durationLabel = "1 mnt";
    private int profitPercent = 85;
====
    private double stakeAmount = 10.0;
    private final int[] DURATIONS = {60, 300, 900, 1800, 3600};
    private final String[] DURATION_LABELS = {"1 mnt", "5 mnt", "15 mnt", "30 mnt", "1 jam"};
    private int durationIndex = 0;
    private int durationSeconds = 60;
    private String durationLabel = "1 mnt";
    private int profitPercent = 85;
>>>>
```

- [ ] **Step 2: Add click listeners for btnDurationMinus and btnDurationPlus**

```java
<<<<
        b.btnDuration.setOnClickListener(x -> {
            DurationBottomSheetFragment sheet = new DurationBottomSheetFragment();
            sheet.setCallback((sec, label) -> {
                durationSeconds = sec; durationLabel = label;
                b.tvDuration.setText(label);
            });
            sheet.show(getChildFragmentManager(), "duration");
        });
====
        b.btnDuration.setOnClickListener(x -> {
            DurationBottomSheetFragment sheet = new DurationBottomSheetFragment();
            sheet.setCallback((sec, label) -> {
                durationSeconds = sec; durationLabel = label;
                b.tvDuration.setText(label);
                // Update index for stepper sync
                for(int i=0; i<DURATIONS.length; i++) {
                    if(DURATIONS[i] == sec) { durationIndex = i; break; }
                }
            });
            sheet.show(getChildFragmentManager(), "duration");
        });

        b.btnDurationMinus.setOnClickListener(x -> {
            if (durationIndex > 0) {
                durationIndex--;
                updateDurationUI();
            }
        });
        b.btnDurationPlus.setOnClickListener(x -> {
            if (durationIndex < DURATIONS.length - 1) {
                durationIndex++;
                updateDurationUI();
            }
        });
>>>>
```

- [ ] **Step 3: Add updateDurationUI helper method**

```java
    private void updateDurationUI() {
        durationSeconds = DURATIONS[durationIndex];
        durationLabel = DURATION_LABELS[durationIndex];
        b.tvDuration.setText(durationLabel);
    }
```

- [ ] **Step 4: Commit**
```bash
git add app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java
git commit -m "feat(terminal): implement duration stepper buttons (+/-)"
```

### Task 3: Fix HomeFragment Chip Labels and Logic

**Files:**
- Modify: `app/src/main/res/layout/fragment_home.xml`
- Modify: `app/src/main/java/com/diellabs/frexa/ui/home/HomeFragment.java`

- [ ] **Step 1: Update chip labels in fragment_home.xml**

```xml
<<<<
                    <com.google.android.material.chip.Chip
                        android:id="@+id/chip_fixed_time"
                        style="@style/Widget.Material3.Chip.Filter"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Fixed Time"
                        android:checked="true" />

                    <com.google.android.material.chip.Chip
                        android:id="@+id/chip_forex"
                        style="@style/Widget.Material3.Chip.Filter"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Forex" />

                    <com.google.android.material.chip.Chip
                        android:id="@+id/chip_stocks"
                        style="@style/Widget.Material3.Chip.Filter"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Stocks" />
====
                    <com.google.android.material.chip.Chip
                        android:id="@+id/chip_trending"
                        style="@style/Widget.Material3.Chip.Filter"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Trending"
                        android:checked="true" />

                    <com.google.android.material.chip.Chip
                        android:id="@+id/chip_gainers"
                        style="@style/Widget.Material3.Chip.Filter"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Gainers" />

                    <com.google.android.material.chip.Chip
                        android:id="@+id/chip_losers"
                        style="@style/Widget.Material3.Chip.Filter"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Losers" />
>>>>
```

- [ ] **Step 2: Update chip IDs and logic in HomeFragment.java**

```java
<<<<
        b.chipGroupAssets.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                int index = 0;
                if (checkedId == R.id.chip_forex) index = 1;
                else if (checkedId == R.id.chip_stocks) index = 2;
                filterCoins(index);
            }
        });

        cryptoVm.coinList.observe(getViewLifecycleOwner(), coins -> {
            allCoins = coins;
            List<CoinMarket> movers = coins.stream()
                    .sorted((a, c2) -> Double.compare(c2.priceChangePercentage24h, a.priceChangePercentage24h))
                    .limit(5).collect(Collectors.toList());
            moverAdapter.setData(movers);
            
            int index = 0;
            if (b.chipGroupAssets.getCheckedChipId() == R.id.chip_forex) index = 1;
            else if (b.chipGroupAssets.getCheckedChipId() == R.id.chip_stocks) index = 2;
            filterCoins(index);
====
        b.chipGroupAssets.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                int index = 0;
                if (checkedId == R.id.chip_gainers) index = 1;
                else if (checkedId == R.id.chip_losers) index = 2;
                filterCoins(index);
            }
        });

        cryptoVm.coinList.observe(getViewLifecycleOwner(), coins -> {
            allCoins = coins;
            List<CoinMarket> movers = coins.stream()
                    .sorted((a, c2) -> Double.compare(c2.priceChangePercentage24h, a.priceChangePercentage24h))
                    .limit(5).collect(Collectors.toList());
            moverAdapter.setData(movers);
            
            int index = 0;
            if (b.chipGroupAssets.getCheckedChipId() == R.id.chip_gainers) index = 1;
            else if (b.chipGroupAssets.getCheckedChipId() == R.id.chip_losers) index = 2;
            filterCoins(index);
>>>>
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/layout/fragment_home.xml app/src/main/java/com/diellabs/frexa/ui/home/HomeFragment.java
git commit -m "fix(home): update asset filter chip labels to Trending, Gainers, Losers"
```

### Task 4: Verification

- [ ] **Step 1: Compile the project**
Run: `./gradlew assembleDebug`
Expected: SUCCESS

- [ ] **Step 2: Check for any lint or type errors in the modified files.**

---
