# Supporting Layouts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement more complex layouts and item views for Transaksi, Bantuan, Notifikasi, and Deposit features to match the UI redesign specs.

**Architecture:** Update XML layouts using existing themes, colors, and drawables (`bg_surface`, `btn_cta_grad`, `ic_*`). Use Material components where appropriate.

**Tech Stack:** Android XML Layouts, Material Design 3.

---

### Task 1: Update Transaksi Fragment Layout

**Files:**
- Modify: `app/src/main/res/layout/fragment_transaksi.xml`

- [ ] **Step 1: Update `fragment_transaksi.xml` with Tab row and Summary row**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:background="@color/frx_background">

    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="vertical" android:paddingBottom="80dp">

        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="Transaksi" android:textStyle="bold" android:textSize="22sp"
            android:layout_marginStart="16dp" android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp" android:textColor="@color/frx_text_1" />

        <!-- Tab Row -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:paddingHorizontal="16dp"
            android:layout_marginBottom="16dp">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Fixed Time"
                android:background="@drawable/bg_chip_active"
                android:paddingHorizontal="16dp"
                android:paddingVertical="8dp"
                android:textColor="@color/white"
                android:textSize="14sp"
                android:layout_marginEnd="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Forex"
                android:background="@drawable/bg_chip_inactive"
                android:paddingHorizontal="16dp"
                android:paddingVertical="8dp"
                android:textColor="@color/frx_text_2"
                android:textSize="14sp"
                android:layout_marginEnd="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Stocks"
                android:background="@drawable/bg_chip_inactive"
                android:paddingHorizontal="16dp"
                android:paddingVertical="8dp"
                android:textColor="@color/frx_text_2"
                android:textSize="14sp" />
        </LinearLayout>

        <!-- Summary Row -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:background="@drawable/bg_surface"
            android:layout_marginHorizontal="16dp"
            android:padding="16dp"
            android:layout_marginBottom="24dp">
            
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Nominal total"
                    android:textColor="@color/frx_text_2"
                    android:textSize="12sp" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="$1,240.50"
                    android:textColor="@color/frx_text_1"
                    android:textStyle="bold"
                    android:textSize="16sp" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="end">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Profit dan rugi"
                    android:textColor="@color/frx_text_2"
                    android:textSize="12sp" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="+$45.20"
                    android:textColor="@color/frx_green"
                    android:textStyle="bold"
                    android:textSize="16sp" />
            </LinearLayout>
        </LinearLayout>

        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="Posisi Terbuka" android:textStyle="bold" android:textSize="16sp"
            android:layout_marginStart="16dp" android:layout_marginBottom="8dp"
            android:textColor="@color/frx_text_1" />

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rv_open_trades"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
            android:nestedScrollingEnabled="false" />

        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="Riwayat" android:textStyle="bold" android:textSize="16sp"
            android:layout_marginStart="16dp" android:layout_marginTop="16dp"
            android:layout_marginBottom="8dp"
            android:textColor="@color/frx_text_1" />

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rv_history"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
            android:nestedScrollingEnabled="false" />

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/fragment_transaksi.xml
git commit -m "feat(ui): update transaksi fragment with tabs and summary"
```

---

### Task 2: Update Bantuan Fragment Layout

**Files:**
- Modify: `app/src/main/res/layout/fragment_bantuan.xml`

- [ ] **Step 1: Update `fragment_bantuan.xml` with 2x2 grid and correct icons**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:padding="16dp"
    android:background="@color/frx_background">

    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:text="Bantuan" android:textStyle="bold" android:textSize="24sp"
        android:layout_marginBottom="24dp" android:layout_marginTop="8dp"
        android:textColor="@color/frx_text_1" />

    <androidx.gridlayout.widget.GridLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:columnCount="2"
        app:rowCount="2">

        <!-- Card 1: Question Circle -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/btn_support"
            android:layout_width="0dp"
            android:layout_height="120dp"
            app:layout_columnWeight="1"
            android:layout_margin="6dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="0dp"
            app:strokeWidth="0dp">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@drawable/bg_surface"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="16dp">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_question_circle"
                    app:tint="@color/frx_primary"
                    android:layout_marginBottom="12dp" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Dukungan 24/7"
                    android:textStyle="bold"
                    android:textSize="14sp"
                    android:textColor="@color/frx_text_1"
                    android:gravity="center" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Card 2: Info Circle -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/btn_help"
            android:layout_width="0dp"
            android:layout_height="120dp"
            app:layout_columnWeight="1"
            android:layout_margin="6dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="0dp"
            app:strokeWidth="0dp">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@drawable/bg_surface"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="16dp">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_info_circle"
                    app:tint="@color/frx_primary"
                    android:layout_marginBottom="12dp" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Pusat Bantuan"
                    android:textStyle="bold"
                    android:textSize="14sp"
                    android:textColor="@color/frx_text_1"
                    android:gravity="center" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Card 3: Graduation -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/btn_education"
            android:layout_width="0dp"
            android:layout_height="120dp"
            app:layout_columnWeight="1"
            android:layout_margin="6dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="0dp"
            app:strokeWidth="0dp">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@drawable/bg_surface"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="16dp">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_graduation"
                    app:tint="@color/frx_primary"
                    android:layout_marginBottom="12dp" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Edukasi Trading"
                    android:textStyle="bold"
                    android:textSize="14sp"
                    android:textColor="@color/frx_text_1"
                    android:gravity="center" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Card 4: Chart Line -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/btn_tutorial"
            android:layout_width="0dp"
            android:layout_height="120dp"
            app:layout_columnWeight="1"
            android:layout_margin="6dp"
            app:cardCornerRadius="16dp"
            app:cardElevation="0dp"
            app:strokeWidth="0dp">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@drawable/bg_surface"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="16dp">
                <ImageView
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_chart_line"
                    app:tint="@color/frx_primary"
                    android:layout_marginBottom="12dp" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Tutorial Trading"
                    android:textStyle="bold"
                    android:textSize="14sp"
                    android:textColor="@color/frx_text_1"
                    android:gravity="center" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </androidx.gridlayout.widget.GridLayout>
</LinearLayout>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/fragment_bantuan.xml
git commit -m "feat(ui): update bantuan fragment with grid and icons"
```

---

### Task 3: Update Notification Fragment Layout

**Files:**
- Modify: `app/src/main/res/layout/fragment_notification.xml`

- [ ] **Step 1: Update `fragment_notification.xml` with Title and Toggle list**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/frx_background">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">
        
        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="Notifikasi" android:textStyle="bold" android:textSize="24sp"
            android:textColor="@color/frx_text_1" />

        <ImageView
            android:id="@+id/btn_settings"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true"
            android:src="@drawable/ic_gear"
            app:tint="@color/frx_text_2" />
    </RelativeLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Mock 8 items -->
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_1" />
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_2" />
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_3" />
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_4" />
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_5" />
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_6" />
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_7" />
            <include layout="@layout/item_notification_toggle" android:id="@+id/notif_8" />

        </LinearLayout>
    </ScrollView>

</LinearLayout>
```

- [ ] **Step 2: Create `item_notification_toggle.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingVertical="12dp">

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Notifikasi Harga"
            android:textStyle="bold"
            android:textSize="16sp"
            android:textColor="@color/frx_text_1" />
            
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Dapatkan info saat harga menyentuh target"
            android:textSize="12sp"
            android:textColor="@color/frx_text_2" />
    </LinearLayout>

    <com.google.android.material.switchmaterial.SwitchMaterial
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:checked="true" />

</LinearLayout>
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/layout/fragment_notification.xml app/src/main/res/layout/item_notification_toggle.xml
git commit -m "feat(ui): update notification fragment with settings icon and toggles"
```

---

### Task 4: Update Deposit Bottom Sheet Layout

**Files:**
- Modify: `app/src/main/res/layout/bottom_sheet_deposit.xml`

- [ ] **Step 1: Update `bottom_sheet_deposit.xml` with refined flow and gradient button**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:padding="24dp"
    android:background="@drawable/bg_surface">

    <View android:layout_width="40dp" android:layout_height="4dp"
        android:layout_gravity="center_horizontal" android:layout_marginBottom="20dp"
        android:background="@color/frx_text_2" android:alpha="0.2" />

    <!-- Step Container -->
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <!-- Step 0: Main (Pilih Metode & Nominal) -->
        <LinearLayout android:id="@+id/vg_main"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:orientation="vertical">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="Deposit Saldo" android:textStyle="bold" android:textSize="20sp"
                android:textColor="@color/frx_text_1" android:layout_marginBottom="16dp" />
            
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="Metode Pembayaran" android:textStyle="bold" android:textSize="14sp"
                android:textColor="@color/frx_text_1" android:layout_marginBottom="8dp" />
            
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_surface_2"
                android:padding="16dp"
                android:layout_marginBottom="16dp"
                android:gravity="center_vertical">
                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Virtual Account BCA"
                    android:textColor="@color/frx_text_1" />
                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@drawable/ic_chevron_right"
                    app:tint="@color/frx_text_2" />
            </LinearLayout>

            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="Nominal Deposit" android:textStyle="bold" android:textSize="14sp"
                android:textColor="@color/frx_text_1" android:layout_marginBottom="8dp" />

            <EditText
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:background="@drawable/bg_surface_2"
                android:paddingHorizontal="16dp"
                android:hint="Min. $10"
                android:inputType="number"
                android:textColor="@color/frx_text_1"
                android:layout_marginBottom="16dp" />
            
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="Promo & Voucher" android:textStyle="bold" android:textSize="14sp"
                android:textColor="@color/frx_text_1" android:layout_marginBottom="8dp" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_surface_2"
                android:padding="16dp"
                android:gravity="center_vertical">
                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Gunakan Voucher"
                    android:textColor="@color/frx_text_2" />
                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@drawable/ic_chevron_right"
                    app:tint="@color/frx_text_2" />
            </LinearLayout>
        </LinearLayout>

        <!-- Other steps (simplified for now as they are dynamically shown) -->
        <LinearLayout android:id="@+id/vg_nominal"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:orientation="vertical" android:visibility="gone">
            <!-- Content for nominal picker -->
        </LinearLayout>

        <LinearLayout android:id="@+id/vg_qr"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:orientation="vertical" android:gravity="center" android:visibility="gone">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="Scan QR" android:textStyle="bold" android:textSize="18sp"
                android:layout_marginBottom="16dp" />
            <ImageView
                android:layout_width="200dp"
                android:layout_height="200dp"
                android:src="@drawable/ic_copy"
                android:layout_marginBottom="16dp" />
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="Selesaikan pembayaran dalam 15:00"
                android:textColor="@color/frx_text_2" android:gravity="center" />
        </LinearLayout>

    </FrameLayout>

    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:layout_marginTop="24dp">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_close"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="wrap_content" android:layout_height="56dp"
            android:text="Tutup" android:layout_marginEnd="8dp"
            android:textColor="@color/frx_text_1"
            app:strokeColor="@color/frx_text_2" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_next"
            android:layout_width="0dp" android:layout_height="56dp"
            android:layout_weight="1" android:text="Lanjutkan"
            android:backgroundTint="@android:color/transparent"
            android:background="@drawable/btn_cta_grad"
            app:backgroundTint="@null"
            android:textColor="@color/white" />

    </LinearLayout>
</LinearLayout>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/bottom_sheet_deposit.xml
git commit -m "feat(ui): update deposit bottom sheet with refined flow and styles"
```

---

### Task 5: Update Recycler Item Layouts

**Files:**
- Modify: `app/src/main/res/layout/item_open_trade.xml`
- Modify: `app/src/main/res/layout/item_history_trade.xml`
- Modify: `app/src/main/res/layout/item_coin.xml` (Verify)

- [ ] **Step 1: Update `item_open_trade.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp" android:layout_marginBottom="10dp"
    app:cardCornerRadius="14dp"
    app:cardElevation="0dp"
    app:strokeWidth="0dp">
    
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:padding="14dp" 
        android:gravity="center_vertical"
        android:background="@drawable/bg_surface">
        
        <ImageView android:id="@+id/iv_coin"
            android:layout_width="36dp" android:layout_height="36dp"
            android:layout_marginEnd="12dp"
            android:contentDescription="Coin" />
            
        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1" android:orientation="vertical">
            <TextView android:id="@+id/tv_symbol"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textStyle="bold" android:textSize="15sp"
                android:textColor="@color/frx_text_1" />
            <TextView android:id="@+id/tv_direction"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textSize="12sp"
                android:textColor="@color/frx_green"
                android:text="Beli · $500.00" />
        </LinearLayout>
        
        <LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:orientation="vertical" android:gravity="end">
            <TextView android:id="@+id/tv_countdown"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textSize="13sp" android:fontFamily="monospace"
                android:textColor="@color/frx_text_2"
                android:text="00:45" />
            <TextView android:id="@+id/tv_pnl"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textStyle="bold" android:textSize="14sp"
                android:textColor="@color/frx_green"
                android:text="+$12.50 (2.5%)" />
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 2: Update `item_history_trade.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp" android:layout_marginBottom="10dp"
    app:cardCornerRadius="14dp"
    app:cardElevation="0dp"
    app:strokeWidth="0dp"
    android:clickable="true" android:focusable="true">
    
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:padding="14dp" 
        android:gravity="center_vertical"
        android:background="@drawable/bg_surface">
        
        <ImageView android:id="@+id/iv_coin"
            android:layout_width="36dp" android:layout_height="36dp"
            android:layout_marginEnd="12dp"
            android:contentDescription="Coin" />
            
        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1" android:orientation="vertical">
            <TextView android:id="@+id/tv_symbol"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textStyle="bold" android:textSize="15sp"
                android:textColor="@color/frx_text_1" />
            <TextView android:id="@+id/tv_direction"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textSize="12sp"
                android:textColor="@color/frx_red"
                android:text="Jual · $200.00" />
        </LinearLayout>
        
        <LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:orientation="vertical" android:gravity="end">
            <TextView android:id="@+id/tv_dur"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textSize="12sp" android:textColor="@color/frx_text_2"
                android:text="1m" />
            <TextView android:id="@+id/tv_pnl"
                android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textStyle="bold" android:textSize="14sp"
                android:textColor="@color/frx_green"
                android:text="+$160.00" />
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 3: Verify `item_coin.xml`**
(Already looks good based on previous read, uses `bg_surface` and correct styles)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/item_open_trade.xml app/src/main/res/layout/item_history_trade.xml
git commit -m "feat(ui): update trade recycler items with refined styles"
```
