# Lab 11a: Gait and Activity Tracker Using The Accelerometer Sensor

**University:** Universiti Teknikal Malaysia Melaka (UTeM) <br>
**Faculty:** Fakulti Teknologi Maklumat dan Komunikasi (FTMK) / Faculty of Information and Communications Technology <br>
**Subject:** Mobile Application Development (BITP 3453) <br>
**Lab Assignment:** Lab 11a

## 📌 Project Overview
This repository provides a suggested solution for an Android application designed to detect user movement and gait patterns. The project explores the Android `SensorManager` API, specifically comparing hardware-abstracted sensors against raw inertial data processing.

### Objectives
* **Implement High-Level Sensors:** Interface with `TYPE_STEP_COUNTER` and `TYPE_STEP_DETECTOR`.
* **Signal Processing:** Extract raw data from `TYPE_ACCELEROMETER` to calculate motion magnitude.
* **Gait Logic:** Develop a threshold-based peak detection algorithm for manual step counting.
* **Comparison:** Analyze the accuracy and power efficiency of different sensing methods.

---

## 🛠 Features

### 1. Cumulative Step Counter (`TYPE_STEP_COUNTER`)
* **Function:** Returns the total number of steps since the last system reboot.
* **Logic:** Since this sensor cannot be reset programmatically, the app implements a "Session Offset" logic. Upon clicking "Start," the app captures the current sensor value as a baseline and subtracts it from all subsequent readings.

### 2. Real-Time Step Detector (`TYPE_STEP_DETECTOR`)
* **Function:** Triggers an event the moment a step is recognized.
* **Logic:** Ideal for immediate UI feedback, such as a "footprint" animation or a haptic (vibration) pulse for every step taken.

### 3. Manual Gait Analysis (`TYPE_ACCELEROMETER`)
* **Function:** Measures raw acceleration forces on the $x$, $y$, and $z$ axes.
* **Algorithm:** * **Vector Magnitude:** To remain independent of phone orientation, we calculate the Euclidean Norm:
    $$||A|| = \sqrt{x^2 + y^2 + z^2}$$
    * **Peak Detection:** The signal is processed to identify spikes that cross a dynamic threshold (typically $11.5 \text{ to } 12.5 \, m/s^2$), signaling a physical step.



---

## 📱 Technical Implementation

### Permissions
As of Android 10 (API level 29), users must grant physical activity permissions.
```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
```

### Core Logic: The Sensor Pipeline
The application follows the standard Android Sensor Lifecycle to ensure battery efficiency and data accuracy:

1.  **Request Permissions:** Check and request `ACTIVITY_RECOGNITION` at runtime.
2.  **Register:** Initialize `sensorManager.registerListener()` during the `onResume()` or `onStart()` phase.
3.  **Listen:** Capture data packets in `onSensorChanged()`.
4.  **Process:** Apply the Euclidean Norm to accelerometer data to isolate movement from gravity.
5.  **Unregister:** Call `sensorManager.unregisterListener()` in `onPause()` to prevent the sensor from draining the battery while the app is in the background.



---

## 🚀 Getting Started

### 1. Prerequisites
* **Android Studio:** Ladybug (2024.2.1) or newer recommended.
* **Minimum SDK:** API 26 (Android 8.0) - *Note: Step sensors require hardware support.*
* **Target SDK:** API 34+ (Android 14).

### 2. Installation
1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/](https://github.com/)[Your-Username]/[Your-Repo-Name].git
    ```
2.  **Sync Project:** Open the project in Android Studio and let Gradle sync.
3.  **Physical Device Required:** While emulators can simulate basic motion, hardware-backed sensors (`TYPE_STEP_COUNTER`) often require a physical device for accurate testing.

---

## 📊 Lab Evaluation Tasks

To successfully complete this lab, students should perform the following tests:

| Task | Objective | Expected Result |
| :--- | :--- | :--- |
| **Precision Test** | Walk 50 steps exactly. | Compare `STEP_COUNTER` vs. `ACCELEROMETER` algorithm accuracy. |
| **Noise Filtering** | Shake the phone vigorously in hand. | The Accelerometer count should increase, while the Hardware Step Counter should ignore non-gait motion. |
| **Orientation Test** | Place phone in pocket vs. holding it flat. | The Magnitude formula should ensure steps are counted regardless of phone tilt. |
| **Battery Analysis** | Run the app for 5 minutes in "High Frequency" mode. | Use the Android Profiler to see CPU/Battery spikes from raw accelerometer processing. |



---

## 📂 Project Structure
* `MainActivity.kt`: Handles UI updates and sensor lifecycle management.
* `GaitAnalyzer.kt`: Contains the logic for the threshold-based peak detection algorithm.
* `SensorService.kt`: (Optional) Background service implementation for persistent tracking.

---

## 📝 Academic Integrity & License
This project was developed for academic purposes under the **Mobile Application Development (BITP 3453)** curriculum at the **Universiti Teknikal Malaysia Melaka (UTeM)**. 

Distributed under the **MIT License**. See `LICENSE` for more information.

---
**Maintained by:** 
[Muhammad Faheem Mohd Ezani/faheemezani] <br>
[Mohd Hariz Naim @ Mohayat/mhariznaim]
