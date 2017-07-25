package io.github.httpmattpvaughn.terminallauncher;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;

/**
 *
 *
 *
 */
// TODO- try and refactor this all to a single TextView for performance upgrade
// TBH not really performance issues but something to consider for old devices or something

public class HomeActivity extends AppCompatActivity implements TextView.OnEditorActionListener, SuggestionAdapter.SuggestionInterface {

    private ViewGroup container;
    private EditText currentEditText;
    private String deviceIdentifier;
    private ScrollView scrollView;
    private List<ResolveInfo> installedApplications;
    private RecyclerView suggestionView;
    private TextWatcher suggestionTextWatcher;
    private String lastInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        setDeviceName();

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        container = (ViewGroup) findViewById(R.id.text_container);

        // Create suggestion view now so we only have to do it once
        suggestionView = new RecyclerView(this);
        suggestionView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        suggestionTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = charSequence.toString().toLowerCase();
                // Suggest apps when user types "open" or "o"
                if (input.length() > 1) {
                    String args = input.substring(input.indexOf(' ') + 1).toLowerCase();
                    if (args.length() > 0) {
                        // If the suggestions are already shown, just update them
                        showAppSuggestion(args);
                    } else {
                        hideAppSuggestion();
                    }
                } else {
                    hideAppSuggestion();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        };

        // Add line which user uses to input
        addNewLine();

        parseInput("");
        prepareForNextInput();
        clear();
        addNewLine();

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentEditText.requestFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });


        if (this.installedApplications == null) {
            this.installedApplications = new ArrayList<>();
            MakeAppList makeAppList = new MakeAppList(getPackageManager(), this.installedApplications);
            makeAppList.execute();
        }
    }

    // Create a string to use as name for device
    private void setDeviceName() {
        deviceIdentifier = Build.MODEL.replace(" ", "_").toUpperCase();
        if (deviceIdentifier.length() > 10) {
            deviceIdentifier = deviceIdentifier.substring(0, 10);
        }
    }

    // Hide the suggestion box
    private void hideAppSuggestion() {
        container.removeView(suggestionView);
    }


    // TODO_ first damn input doesn't appaer for whatever damn reason
    // Adds an empty line to the bottom where user can write input
    private void addNewLine() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView userName = new TextView(this, null, R.style.TerminalTextView);
        userName.setTypeface(Typeface.MONOSPACE);
        userName.setText("/" + deviceIdentifier + "/: ");
        linearLayout.addView(userName);

        // TODO- figure out why this opens up
        // TODO- figure out which of the IME/singleline things is actually necessary
        currentEditText = new EditText(this, null, R.style.TerminalEditText);
        currentEditText.setImeOptions(IME_ACTION_SEND);
        currentEditText.setSingleLine();
        currentEditText.setTypeface(Typeface.MONOSPACE);
        currentEditText.setFocusableInTouchMode(true);
        currentEditText.requestFocus();
        linearLayout.addView(currentEditText);

        container.addView(linearLayout);

        currentEditText.setOnEditorActionListener(this);
        currentEditText.addTextChangedListener(suggestionTextWatcher);
    }

    // prints a string as a single line on the screen
    private void printLine(String string) {
        TextView printedLine = new TextView(this, null, R.style.TerminalTextView);
        printedLine.setTypeface(Typeface.MONOSPACE);
        printedLine.setText(String.format("   %s", string));
        container.addView(printedLine);
    }

    // Prints a line of text which wraps around to left side
    private void printWrap(String string) {
        TextView printedLine = new TextView(this, null, R.style.TerminalTextView);
        printedLine.setMaxLines(100);
        printedLine.setTypeface(Typeface.MONOSPACE);
        printedLine.setText(String.format("   %s", string));
        container.addView(printedLine);
    }

    // Responds to device
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        String input = currentEditText.getText().toString();
        int action = -2;
        if (keyEvent != null) {
            action = keyEvent.getAction();
        }
        System.out.println(keyEvent + " " + action);
        if (actionId == IME_ACTION_SEND || keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            // Parse the input and respond accordingly
            hideAppSuggestion();
            parseInput(input);
            prepareForNextInput();
            scrollToBottom();
        }
        return true;
    }

    // Scrolls to the bottom of the terminal
    private void scrollToBottom() {
        container.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
                // Request focus in case it scrolls down while user
                // is currently adding input
                currentEditText.requestFocus();
            }
        }, 50);
    }

    // Prepares the editor for thge next user input
    public void prepareForNextInput() {
        // Make previous query uneditable
        if (currentEditText != null) {
            currentEditText.setEnabled(false);
        }

        // Add line for user to input next command
        addNewLine();
    }

    // Shows a list of suggestions of apps given the start of an argument
    private void showAppSuggestion(String args) {
        List<String> matchedApps = matchApps(args);
        SuggestionAdapter suggestionAdapter = new SuggestionAdapter(matchedApps, this);
        suggestionView.setAdapter(suggestionAdapter);
        // If the suggestionview is already in the container, don't add again
        if (suggestionView.getParent() == null) {
            container.addView(suggestionView);
        }
        scrollToBottom();
    }

    // Parse input into command and arguments
    // Executes commands
    public void parseInput(String input) {
        String command;
        String args = null;
        if (input.contains(" ")) {
            command = input.substring(0, input.indexOf(' '));
            args = input.substring(input.indexOf(' ') + 1);
        } else {
            command = input;
        }
        switch (command.toLowerCase()) {
            case "help":
            case "?":
                printHelpMessage();
                break;
            case "ls":
            case "apps":
                printInstalledApps();
                break;
            case "\uD83D\uDCA6":
            case "\uD83C\uDF46":
                openApp("snapchat");
                break;
            case "open":
            case "o":
                if (args == null || args.length() == 0) {
                    printWrap("Incorrect command. Do it like this: \"open contacts\"");
                } else {
                    openApp(args.toLowerCase().trim());
                }
                break;
            case "uninstall":
            case "u":
                uninstall(args.toLowerCase());
                break;
            case "batt":
            case "battery":
                printBattery();
                break;
            case "redo":
            case ".":
                if (lastInput != null) {
                    String lastCommand = null;
                    if (lastInput.contains(" ")) {
                        lastCommand = lastInput.substring(0, lastInput.indexOf(' '));
                    } else {
                        lastCommand = lastInput;
                    }
                    if (!lastCommand.equals("redo") && !lastCommand.equals(".")) {
                        parseInput(lastInput);
                    } else {
                        printLine("Unable to redo");
                    }
                }
                break;
            case "time":
            case "date":
                printTime();
                break;
            case "clear":
                clear();
                break;
            case "google":
            case "g":
                printWrap("Searching google for \"" + args + "\"");
                args = args.replace(" ", "+");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/search?q=" + args));
                startActivity(browserIntent);
                break;
            default:
                openApp(input);
                break;
        }
        lastInput = input;
    }

    // Prints the device battery stats (percentage, whether its charging)
    private void printBattery() {
        String isCharging = "";
        if (!isPhonePluggedIn(this)) {
            isCharging = "NOT ";
        }
        printLine(getBatteryPercentage(this) + "%, " + isCharging + "charging");
    }

    // Returns an integer representing the percentage level of the device battery
    // https://stackoverflow.com/questions/3291655/get-battery-level-and-state-in-android
    public static int getBatteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    // Returns boolean about whether phone is charging
    // https://stackoverflow.com/questions/6243452/how-to-know-if-the-phone-is-charging
    public static boolean isPhonePluggedIn(Context context) {
        boolean charging = false;

        final Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = status == BatteryManager.BATTERY_STATUS_CHARGING;

        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (batteryCharge) charging = true;
        if (usbCharge) charging = true;
        if (acCharge) charging = true;

        return charging;
    }

    // Prints the current time and date
    private void printTime() {
        String time = DateFormat.getDateTimeInstance().format(new Date());
        printLine(time);
    }

    // Removes all views from container
    private void clear() {
        container.removeAllViews();
    }

    // Returns list of ResolveInfos for apps which contain the
    // app name specified by the user
    @NonNull
    private List<String> matchApps(String app) {
        List<String> matchedApps = new ArrayList<>();
        for (ResolveInfo info : getInstalledApps()) {
            String actualAppName = info.loadLabel(getPackageManager()).toString().toLowerCase();
            if (actualAppName.startsWith(app)) {
                matchedApps.add(info.loadLabel(getPackageManager()).toString());
            }
        }
        return matchedApps;
    }

    // Gets list of installed apps
    private List<ResolveInfo> getInstalledApps() {
        if (this.installedApplications == null || this.installedApplications.size() == 0) {
            return this.installedApplications = Utils.makeAppList(getPackageManager());
        }
        return this.installedApplications;
    }

    // Opens an app if search by user matches the app
    // Warns user if search matches more than one app
    // --> parameter app name is trimmed and lowercase
    private void openApp(String appName) {
        // Hide any app suggestions
        hideAppSuggestion();

        // Get package name from app name
        List<ResolveInfo> appsFound = new ArrayList<>();
        for (ResolveInfo info : getInstalledApps()) {
            String actualAppName = info.loadLabel(getPackageManager()).toString().toLowerCase();
            if (actualAppName.contains(appName)) {
                if (actualAppName.equalsIgnoreCase(appName)) {
                    printLine("Opening " + actualAppName);
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
                    startActivity(launchIntent);
                    return;
                }
                appsFound.add(info);
            }
        }
        if (appsFound.size() == 0) {
            printLine("No app found with name " + appName);
        } else if (appsFound.size() == 1) {
            try {
                printLine("Opening " + appsFound.get(0).loadLabel(getPackageManager()));
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appsFound.get(0).activityInfo.packageName);
                startActivity(launchIntent);
            } catch (NullPointerException npe) {
                printLine("Error opening " + appName + ", try again with a different search");
            }
        } else if (appsFound.size() > 1) {
            printWrap("Found multiple apps matching that request: ");
            for (ResolveInfo appInfo : appsFound) {
                printLine(appInfo.loadLabel(getPackageManager()).toString());
            }
        }
    }

    // Prints a message listing all possible commands
    private void printHelpMessage() {
        printWrap("OPEN (O)  Opens an app");
        printWrap("APPS      Prints all installed apps");
        printWrap("CLEAR     Clears terminal screen");
        printWrap("TIME      Prints current date/time");
        printWrap("BATT      Prints battery status");
        printWrap("U         Uninstalls an app");
        printWrap("REDO (.)  Repeats last command");
        printWrap("G         Googles following text");
    }

    // Prints a list of all installed apps
    private void printInstalledApps() {
        List<String> orderedList = new ArrayList<String>();
        for (ResolveInfo info : installedApplications) {
            orderedList.add((String) info.loadLabel(getPackageManager()));
        }
        Collections.sort(orderedList);

        for (String string : orderedList) {
            printLine(string);
        }
    }

    // Uninstalls an app which matches a string
    private void uninstall(String app) {
        String packageName = findPackageName(app);
        if (packageName != null) {
            printLine("Uninstalling " + app);
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        } else {
            printLine("Can't find app with name " + app);
        }
    }

    // Finds a package name for an app given its app name
    private String findPackageName(String app) {
        List<ResolveInfo> appsFound = new ArrayList<>();
        for (ResolveInfo info : installedApplications) {
            String packageName = info.loadLabel(getPackageManager()).toString().toLowerCase();
            if (packageName.contains(app)) {
                if (packageName.equalsIgnoreCase(app)) {
                    return packageName;
                }
                appsFound.add(info);
            }
        }
        if (appsFound.size() == 1) {
            return appsFound.get(0).activityInfo.packageName;
        }
        return null;
    }

}
