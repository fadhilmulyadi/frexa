# Removing AI and Login Modules Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove AI Assistant and Login functionality to simplify the application.

**Architecture:** Update `MainActivity` to bypass login checks, remove login and AI fragments from the navigation graph, and delete related files.

**Tech Stack:** Android/Java/Navigation Component

---

### Task 1: Update MainActivity Logic

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/MainActivity.java`

- [ ] **Step 1: Remove login check**

```java
// In MainActivity.java, locate and remove or modify the logic checking isLoggedIn()
// Current:
// if (!prefs.isLoggedIn()) navController.navigate(R.id.loginFragment);
// Change to:
// // Removed login check to bypass login screen
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/diellabs/frexa/MainActivity.java
git commit -m "refactor(nav): remove login check in MainActivity"
```

### Task 2: Update Navigation Graph

**Files:**
- Modify: `app/src/main/res/navigation/nav_graph.xml`

- [ ] **Step 1: Remove fragments from nav_graph.xml**

```xml
<!-- In nav_graph.xml, remove these blocks: -->
<!--
    <fragment android:id="@+id/loginFragment"
        android:name="com.diellabs.frexa.ui.login.LoginFragment">
        ...
    </fragment>

    <fragment android:id="@+id/aiAssistantFragment"
        android:name="com.diellabs.frexa.ui.ai.AiAssistantFragment" />
-->
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/navigation/nav_graph.xml
git commit -m "refactor(nav): remove login and ai fragments from nav_graph"
```

### Task 3: Delete AI and Login Files

**Files:**
- Delete: `app/src/main/java/com/diellabs/frexa/ui/ai/AiAssistantFragment.java`
- Delete: `app/src/main/java/com/diellabs/frexa/ui/login/LoginFragment.java`
- Delete: `app/src/main/res/layout/fragment_ai_assistant.xml`
- Delete: `app/src/main/res/layout/fragment_login.xml`

- [ ] **Step 1: Delete files**

```bash
rm app/src/main/java/com/diellabs/frexa/ui/ai/AiAssistantFragment.java
rm app/src/main/java/com/diellabs/frexa/ui/login/LoginFragment.java
rm app/src/main/res/layout/fragment_ai_assistant.xml
rm app/src/main/res/layout/fragment_login.xml
```

- [ ] **Step 2: Commit**

```bash
git add .
git commit -m "refactor(ui): delete login and ai fragment code and layouts"
```

### Task 4: Cleanup & Verification

**Files:**
- Modify: `app/src/main/res/menu/bottom_nav_menu.xml` (if necessary)

- [ ] **Step 1: Check bottom_nav_menu.xml for AI reference**

Check `app/src/main/res/menu/bottom_nav_menu.xml`. If it contains an item for AI, remove it.

- [ ] **Step 2: Final Verification**

Run the build to ensure no compilation errors:
```bash
./gradlew assembleDebug
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/menu/bottom_nav_menu.xml
git commit -m "refactor(menu): remove ai from bottom navigation"
```
