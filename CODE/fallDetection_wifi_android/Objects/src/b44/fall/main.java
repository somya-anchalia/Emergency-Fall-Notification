package b44.fall;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "b44.fall", "b44.fall.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b44.fall", "b44.fall.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b44.fall.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static long _gpsmintime = 0L;
public static float _gpsmindistance = 0f;
public static boolean _gps_on = false;
public static anywheresoftware.b4a.gps.GPS _gps1 = null;
public static anywheresoftware.b4a.objects.SocketWrapper.ServerSocketWrapper _server = null;
public static anywheresoftware.b4a.objects.SocketWrapper _clientsocket = null;
public static anywheresoftware.b4a.randomaccessfile.AsyncStreams _astreamclient = null;
public static anywheresoftware.b4a.phone.Phone.PhoneSms _sms = null;
public static String _mobile1 = "";
public static String _mobile2 = "";
public static anywheresoftware.b4a.phone.PhoneEvents.SMSInterceptor _intercept = null;
public static int _total = 0;
public static anywheresoftware.b4a.objects.Timer _timer1 = null;
public static String _user = "";
public static String _pass = "";
public static String _ip2 = "";
public static String _ip1 = "";
public anywheresoftware.b4a.objects.ButtonWrapper _btnsend = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnclear = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtlog = null;
public anywheresoftware.b4a.objects.LabelWrapper _lblmsg = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext1 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext2 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txttable = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtguests = null;
public anywheresoftware.b4a.objects.ListViewWrapper _listview1 = null;
public anywheresoftware.b4a.objects.ListViewWrapper _listview2 = null;
public anywheresoftware.b4a.objects.ListViewWrapper _listview3 = null;
public anywheresoftware.b4a.objects.ListViewWrapper _listview4 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtrate = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtquantity = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdadd = null;
public anywheresoftware.b4a.objects.ButtonWrapper _connect1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _connect2 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtip1 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtip2 = null;
public anywheresoftware.b4a.objects.LabelWrapper _ip = null;
public anywheresoftware.b4a.objects.LabelWrapper _iot = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbipatual = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtd1 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtd2 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _btnmanual = null;
public anywheresoftware.b4a.objects.ButtonWrapper _exit = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _btnauto = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnd3on = null;
public anywheresoftware.b4a.objects.LabelWrapper _lblip = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbld1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbld2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnd1on = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnd1off = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnd2on = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnd2off = null;
public anywheresoftware.b4a.objects.LabelWrapper _label1 = null;
public anywheresoftware.b4a.objects.SpinnerWrapper _spinner1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnd3off = null;
public anywheresoftware.b4a.objects.PanelWrapper _panel1 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _radiobutton1 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _radiobutton2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button3 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button4 = null;
public anywheresoftware.b4a.objects.SpinnerWrapper _spinner2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button6 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button5 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button7 = null;
public anywheresoftware.b4a.objects.PanelWrapper _panel2 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtuser = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtpass = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdlogin = null;
public anywheresoftware.b4a.objects.LabelWrapper _label2 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label3 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label1l = null;
public anywheresoftware.b4a.objects.LabelWrapper _label2l = null;
public anywheresoftware.b4a.objects.LabelWrapper _label3l = null;
public anywheresoftware.b4a.objects.ButtonWrapper _table1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _table2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdstatus = null;
public anywheresoftware.b4a.objects.LabelWrapper _label8 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label5 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label6 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label7 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext4 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext5 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext6 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label4 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext3 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _lbllongitude = null;
public anywheresoftware.b4a.objects.EditTextWrapper _lbllatitude = null;
public anywheresoftware.b4a.objects.LabelWrapper _label9 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label10 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdsback = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdsave = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtmobile2 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtmobile1 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtusersave = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btativaclient = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 135;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 137;BA.debugLine="Activity.LoadLayout(\"Login\")";
mostCurrent._activity.LoadLayout("Login",mostCurrent.activityBA);
 //BA.debugLineNum = 138;BA.debugLine="GPS1.Initialize(\"GPS1\")";
_gps1.Initialize("GPS1");
 //BA.debugLineNum = 139;BA.debugLine="If GPS1.GPSEnabled = False Then";
if (_gps1.getGPSEnabled()==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 140;BA.debugLine="ToastMessageShow(\"Please enable the GPS devic";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Please enable the GPS device."+anywheresoftware.b4a.keywords.Common.CRLF+"And press the BACK button",anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 141;BA.debugLine="StartActivity(GPS1.LocationSettingsIntent)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_gps1.getLocationSettingsIntent()));
 };
 //BA.debugLineNum = 144;BA.debugLine="timer1.Initialize(\"Timer1\", 1000) ' start timer";
_timer1.Initialize(processBA,"Timer1",(long) (1000));
 //BA.debugLineNum = 145;BA.debugLine="timer1.Enabled=False";
_timer1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 146;BA.debugLine="GPS_On = True";
_gps_on = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 148;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 164;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 168;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 150;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 151;BA.debugLine="If GPS1.GPSEnabled = False Then";
if (_gps1.getGPSEnabled()==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 152;BA.debugLine="ToastMessageShow(\"Please enable the GPS dev";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Please enable the GPS device.",anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 153;BA.debugLine="StartActivity(GPS1.LocationSettingsIntent)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_gps1.getLocationSettingsIntent()));
 };
 //BA.debugLineNum = 158;BA.debugLine="GPS1.Start(GPSMinTime, GPSMinDistance)";
_gps1.Start(processBA,_gpsmintime,_gpsmindistance);
 //BA.debugLineNum = 162;BA.debugLine="End Sub";
return "";
}
public static String  _astreamclient_newdata(byte[] _buffer) throws Exception{
String _msg = "";
 //BA.debugLineNum = 177;BA.debugLine="Sub AStreamClient_NewData (Buffer() As Byte)";
 //BA.debugLineNum = 178;BA.debugLine="Dim msg As String";
_msg = "";
 //BA.debugLineNum = 180;BA.debugLine="EditText1.Text=\"\"";
mostCurrent._edittext1.setText((Object)(""));
 //BA.debugLineNum = 181;BA.debugLine="EditText1.Text =EditText1.Text & BytesToString(Bu";
mostCurrent._edittext1.setText((Object)(mostCurrent._edittext1.getText()+anywheresoftware.b4a.keywords.Common.BytesToString(_buffer,(int) (0),_buffer.length,"UTF8")));
 //BA.debugLineNum = 182;BA.debugLine="If Not(EditText1.Text.SubString2(0,1)=\"F\")Then";
if (anywheresoftware.b4a.keywords.Common.Not((mostCurrent._edittext1.getText().substring((int) (0),(int) (1))).equals("F"))) { 
 //BA.debugLineNum = 183;BA.debugLine="EditText1.Text=\"\"";
mostCurrent._edittext1.setText((Object)(""));
 }else {
 //BA.debugLineNum = 185;BA.debugLine="timer1.Interval=25000";
_timer1.setInterval((long) (25000));
 //BA.debugLineNum = 186;BA.debugLine="timer1.Enabled=True";
_timer1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 194;BA.debugLine="ToastMessageShow(msg, False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(_msg,anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 196;BA.debugLine="End Sub";
return "";
}
public static String  _btativaclient_click() throws Exception{
 //BA.debugLineNum = 210;BA.debugLine="Sub btAtivaClient_Click";
 //BA.debugLineNum = 215;BA.debugLine="If Not(ClientSocket.IsInitialized) Then";
if (anywheresoftware.b4a.keywords.Common.Not(_clientsocket.IsInitialized())) { 
 //BA.debugLineNum = 216;BA.debugLine="ClientSocket.Initialize(\"ClientSocket\")";
_clientsocket.Initialize("ClientSocket");
 }else {
 //BA.debugLineNum = 218;BA.debugLine="ClientSocket.Close";
_clientsocket.Close();
 //BA.debugLineNum = 219;BA.debugLine="ClientSocket.Initialize(\"ClientSocket\")";
_clientsocket.Initialize("ClientSocket");
 };
 //BA.debugLineNum = 221;BA.debugLine="ClientSocket.Connect(EditText3.Text, EditText5.Te";
_clientsocket.Connect(processBA,mostCurrent._edittext3.getText(),(int)(Double.parseDouble(mostCurrent._edittext5.getText())),(int) (5000));
 //BA.debugLineNum = 224;BA.debugLine="End Sub";
return "";
}
public static String  _btnclear_click() throws Exception{
 //BA.debugLineNum = 207;BA.debugLine="Sub btnClear_Click";
 //BA.debugLineNum = 208;BA.debugLine="End Sub";
return "";
}
public static String  _btnd2off_click() throws Exception{
 //BA.debugLineNum = 332;BA.debugLine="Sub btnD2Off_Click";
 //BA.debugLineNum = 333;BA.debugLine="End Sub";
return "";
}
public static String  _btnd2on_click() throws Exception{
 //BA.debugLineNum = 329;BA.debugLine="Sub btnD2On_Click";
 //BA.debugLineNum = 331;BA.debugLine="End Sub";
return "";
}
public static String  _btnd3off_click() throws Exception{
 //BA.debugLineNum = 339;BA.debugLine="Sub btnD3Off_Click";
 //BA.debugLineNum = 340;BA.debugLine="End Sub";
return "";
}
public static String  _btnd3on_click() throws Exception{
 //BA.debugLineNum = 335;BA.debugLine="Sub btnD3On_Click";
 //BA.debugLineNum = 336;BA.debugLine="End Sub";
return "";
}
public static String  _button1_click() throws Exception{
 //BA.debugLineNum = 346;BA.debugLine="Sub Button1_Click";
 //BA.debugLineNum = 347;BA.debugLine="Panel2.Visible=True";
mostCurrent._panel2.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 348;BA.debugLine="End Sub";
return "";
}
public static String  _button2_click() throws Exception{
 //BA.debugLineNum = 435;BA.debugLine="Sub Button2_Click";
 //BA.debugLineNum = 436;BA.debugLine="EditText1.Text=\"\"";
mostCurrent._edittext1.setText((Object)(""));
 //BA.debugLineNum = 437;BA.debugLine="End Sub";
return "";
}
public static String  _button3_click() throws Exception{
 //BA.debugLineNum = 439;BA.debugLine="Sub Button3_Click";
 //BA.debugLineNum = 440;BA.debugLine="timer1.Enabled=False";
_timer1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 441;BA.debugLine="EditText1.Text=\"\"";
mostCurrent._edittext1.setText((Object)(""));
 //BA.debugLineNum = 442;BA.debugLine="End Sub";
return "";
}
public static String  _clientsocket_connected(boolean _successful) throws Exception{
 //BA.debugLineNum = 226;BA.debugLine="Sub ClientSocket_Connected (Successful As Boolean)";
 //BA.debugLineNum = 227;BA.debugLine="If Successful Then";
if (_successful) { 
 //BA.debugLineNum = 228;BA.debugLine="AStreamClient.Initialize(ClientSocket.Input";
_astreamclient.Initialize(processBA,_clientsocket.getInputStream(),_clientsocket.getOutputStream(),"AStreamClient");
 //BA.debugLineNum = 229;BA.debugLine="ToastMessageShow(\"Successfully connected\", Tru";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Successfully connected",anywheresoftware.b4a.keywords.Common.True);
 }else {
 //BA.debugLineNum = 231;BA.debugLine="ToastMessageShow(\"No connection!\", True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("No connection!",anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 233;BA.debugLine="End Sub";
return "";
}
public static String  _cmdlogin_click() throws Exception{
 //BA.debugLineNum = 350;BA.debugLine="Sub cmdLogin_Click";
 //BA.debugLineNum = 351;BA.debugLine="ReadTextReader";
_readtextreader();
 //BA.debugLineNum = 352;BA.debugLine="If (txtUser.Text =user)Then";
if (((mostCurrent._txtuser.getText()).equals(mostCurrent._user))) { 
 //BA.debugLineNum = 353;BA.debugLine="Panel1.Visible=True";
mostCurrent._panel1.setVisible(anywheresoftware.b4a.keywords.Common.True);
 }else {
 //BA.debugLineNum = 355;BA.debugLine="ToastMessageShow(\"Invalid User\",True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Invalid User",anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 358;BA.debugLine="End Sub";
return "";
}
public static String  _cmdsave_click() throws Exception{
 //BA.debugLineNum = 431;BA.debugLine="Sub cmdSave_Click";
 //BA.debugLineNum = 432;BA.debugLine="WriteTextWriter";
_writetextwriter();
 //BA.debugLineNum = 433;BA.debugLine="End Sub";
return "";
}
public static String  _cmdsback_click() throws Exception{
 //BA.debugLineNum = 427;BA.debugLine="Sub cmdSBack_Click";
 //BA.debugLineNum = 428;BA.debugLine="Panel2.Visible=False";
mostCurrent._panel2.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 429;BA.debugLine="End Sub";
return "";
}
public static String  _exit_click() throws Exception{
 //BA.debugLineNum = 342;BA.debugLine="Sub Exit_Click";
 //BA.debugLineNum = 343;BA.debugLine="Panel1.Visible =False";
mostCurrent._panel1.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 344;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 37;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 40;BA.debugLine="Public user As String";
mostCurrent._user = "";
 //BA.debugLineNum = 41;BA.debugLine="Public pass As String";
mostCurrent._pass = "";
 //BA.debugLineNum = 42;BA.debugLine="Public ip2 As String";
mostCurrent._ip2 = "";
 //BA.debugLineNum = 43;BA.debugLine="Public ip1 As String";
mostCurrent._ip1 = "";
 //BA.debugLineNum = 45;BA.debugLine="Dim btnSend As Button";
mostCurrent._btnsend = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Dim btnClear As Button";
mostCurrent._btnclear = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Dim txtLog As EditText";
mostCurrent._txtlog = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Dim lblMsg As Label";
mostCurrent._lblmsg = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 49;BA.debugLine="Dim EditText1 As EditText";
mostCurrent._edittext1 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 50;BA.debugLine="Dim EditText2 As EditText";
mostCurrent._edittext2 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 51;BA.debugLine="Dim txtTable As EditText";
mostCurrent._txttable = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 52;BA.debugLine="Dim txtGuests As EditText";
mostCurrent._txtguests = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 53;BA.debugLine="Dim listview1 As ListView";
mostCurrent._listview1 = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 54;BA.debugLine="Dim listview2 As ListView";
mostCurrent._listview2 = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 55;BA.debugLine="Dim listview3 As ListView";
mostCurrent._listview3 = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 56;BA.debugLine="Dim listview4 As ListView";
mostCurrent._listview4 = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 57;BA.debugLine="Dim txtRate As EditText";
mostCurrent._txtrate = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 58;BA.debugLine="Dim txtQuantity As EditText";
mostCurrent._txtquantity = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 59;BA.debugLine="Dim cmdAdd As Button";
mostCurrent._cmdadd = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 61;BA.debugLine="Private Connect1 As Button";
mostCurrent._connect1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 62;BA.debugLine="Private Connect2 As Button";
mostCurrent._connect2 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 64;BA.debugLine="Private txtIp1 As EditText";
mostCurrent._txtip1 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 65;BA.debugLine="Private txtIp2 As EditText";
mostCurrent._txtip2 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 67;BA.debugLine="Private IP As Label";
mostCurrent._ip = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 68;BA.debugLine="Private IOT As Label";
mostCurrent._iot = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 69;BA.debugLine="Private lbIpAtual As Label";
mostCurrent._lbipatual = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 71;BA.debugLine="Private txtD1 As EditText";
mostCurrent._txtd1 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 72;BA.debugLine="Private txtD2 As EditText";
mostCurrent._txtd2 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 74;BA.debugLine="Private btnManual As RadioButton";
mostCurrent._btnmanual = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 75;BA.debugLine="Private Exit As Button";
mostCurrent._exit = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 76;BA.debugLine="Private btnAuto As RadioButton";
mostCurrent._btnauto = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 77;BA.debugLine="Private btnD3On As Button";
mostCurrent._btnd3on = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 78;BA.debugLine="Private lblIP As Label";
mostCurrent._lblip = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 79;BA.debugLine="Private lblD1 As Label";
mostCurrent._lbld1 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 80;BA.debugLine="Private lblD2 As Label";
mostCurrent._lbld2 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 81;BA.debugLine="Private btnD1On As Button";
mostCurrent._btnd1on = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 82;BA.debugLine="Private btnD1Off As Button";
mostCurrent._btnd1off = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 83;BA.debugLine="Private btnD2On As Button";
mostCurrent._btnd2on = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 84;BA.debugLine="Private btnD2Off As Button";
mostCurrent._btnd2off = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 85;BA.debugLine="Private Label1 As Label";
mostCurrent._label1 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 86;BA.debugLine="Private Spinner1 As Spinner";
mostCurrent._spinner1 = new anywheresoftware.b4a.objects.SpinnerWrapper();
 //BA.debugLineNum = 87;BA.debugLine="Private btnD3Off As Button";
mostCurrent._btnd3off = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 90;BA.debugLine="Private Panel1 As Panel";
mostCurrent._panel1 = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 91;BA.debugLine="Private RadioButton1 As RadioButton";
mostCurrent._radiobutton1 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 92;BA.debugLine="Private RadioButton2 As RadioButton";
mostCurrent._radiobutton2 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 93;BA.debugLine="Private EditText1 As EditText";
mostCurrent._edittext1 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 94;BA.debugLine="Private Button1 As Button";
mostCurrent._button1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 95;BA.debugLine="Private Button2 As Button";
mostCurrent._button2 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 96;BA.debugLine="Private Button3 As Button";
mostCurrent._button3 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 97;BA.debugLine="Private Button4 As Button";
mostCurrent._button4 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 98;BA.debugLine="Private Spinner2 As Spinner";
mostCurrent._spinner2 = new anywheresoftware.b4a.objects.SpinnerWrapper();
 //BA.debugLineNum = 99;BA.debugLine="Private Button6 As Button";
mostCurrent._button6 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 100;BA.debugLine="Private Button5 As Button";
mostCurrent._button5 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 101;BA.debugLine="Private Button7 As Button";
mostCurrent._button7 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 102;BA.debugLine="Private Panel2 As Panel";
mostCurrent._panel2 = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 103;BA.debugLine="Private txtUser As EditText";
mostCurrent._txtuser = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 104;BA.debugLine="Private txtPass As EditText";
mostCurrent._txtpass = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 105;BA.debugLine="Private cmdLogin As Button";
mostCurrent._cmdlogin = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 106;BA.debugLine="Private Label2 As Label";
mostCurrent._label2 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 107;BA.debugLine="Private Label3 As Label";
mostCurrent._label3 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 108;BA.debugLine="Private Label1L As Label";
mostCurrent._label1l = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 109;BA.debugLine="Private Label2L As Label";
mostCurrent._label2l = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 110;BA.debugLine="Private Label3L As Label";
mostCurrent._label3l = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 111;BA.debugLine="Private Table1 As Button";
mostCurrent._table1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 112;BA.debugLine="Private Table2 As Button";
mostCurrent._table2 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 113;BA.debugLine="Private cmdStatus As Button";
mostCurrent._cmdstatus = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 114;BA.debugLine="Private Label8 As Label";
mostCurrent._label8 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 115;BA.debugLine="Private Label5 As Label";
mostCurrent._label5 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 116;BA.debugLine="Private Label6 As Label";
mostCurrent._label6 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 117;BA.debugLine="Private Label7 As Label";
mostCurrent._label7 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 118;BA.debugLine="Private EditText4 As EditText";
mostCurrent._edittext4 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 119;BA.debugLine="Private EditText5 As EditText";
mostCurrent._edittext5 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 120;BA.debugLine="Private EditText6 As EditText";
mostCurrent._edittext6 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 121;BA.debugLine="Private Label4 As Label";
mostCurrent._label4 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 122;BA.debugLine="Private EditText3 As EditText";
mostCurrent._edittext3 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 123;BA.debugLine="Private lblLongitude As EditText";
mostCurrent._lbllongitude = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 124;BA.debugLine="Private lblLatitude As EditText";
mostCurrent._lbllatitude = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 125;BA.debugLine="Private Label9 As Label";
mostCurrent._label9 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 126;BA.debugLine="Private Label10 As Label";
mostCurrent._label10 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 127;BA.debugLine="Private cmdSBack As Button";
mostCurrent._cmdsback = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 128;BA.debugLine="Private cmdSave As Button";
mostCurrent._cmdsave = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 129;BA.debugLine="Private txtMobile2 As EditText";
mostCurrent._txtmobile2 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 130;BA.debugLine="Private txtMobile1 As EditText";
mostCurrent._txtmobile1 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 131;BA.debugLine="Private txtUserSave As EditText";
mostCurrent._txtusersave = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 132;BA.debugLine="Private btAtivaClient As Button";
mostCurrent._btativaclient = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 133;BA.debugLine="End Sub";
return "";
}
public static String  _gps1_locationchanged(anywheresoftware.b4a.gps.LocationWrapper _location1) throws Exception{
 //BA.debugLineNum = 170;BA.debugLine="Sub GPS1_LocationChanged (Location1 As Location)";
 //BA.debugLineNum = 173;BA.debugLine="lblLatitude.Text = NumberFormat(Location1.Latitu";
mostCurrent._lbllatitude.setText((Object)(anywheresoftware.b4a.keywords.Common.NumberFormat(_location1.getLatitude(),(int) (1),(int) (6))));
 //BA.debugLineNum = 174;BA.debugLine="lblLongitude.Text = NumberFormat(Location1.Longi";
mostCurrent._lbllongitude.setText((Object)(anywheresoftware.b4a.keywords.Common.NumberFormat(_location1.getLongitude(),(int) (1),(int) (6))));
 //BA.debugLineNum = 176;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 16;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 19;BA.debugLine="Dim GPSMinTime As Long								: GPSMinTime = 50";
_gpsmintime = 0L;
 //BA.debugLineNum = 19;BA.debugLine="Dim GPSMinTime As Long								: GPSMinTime = 50";
_gpsmintime = (long) (5000);
 //BA.debugLineNum = 20;BA.debugLine="Dim GPSMinDistance As Float						: GPSMinDistance";
_gpsmindistance = 0f;
 //BA.debugLineNum = 20;BA.debugLine="Dim GPSMinDistance As Float						: GPSMinDistance";
_gpsmindistance = (float) (10);
 //BA.debugLineNum = 21;BA.debugLine="Dim GPS_On As Boolean									: GPS_On = False";
_gps_on = false;
 //BA.debugLineNum = 21;BA.debugLine="Dim GPS_On As Boolean									: GPS_On = False";
_gps_on = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 23;BA.debugLine="Dim GPS1 As GPS";
_gps1 = new anywheresoftware.b4a.gps.GPS();
 //BA.debugLineNum = 24;BA.debugLine="Dim Server As ServerSocket";
_server = new anywheresoftware.b4a.objects.SocketWrapper.ServerSocketWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Dim ClientSocket  As Socket";
_clientsocket = new anywheresoftware.b4a.objects.SocketWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Dim AStreamClient As AsyncStreams";
_astreamclient = new anywheresoftware.b4a.randomaccessfile.AsyncStreams();
 //BA.debugLineNum = 28;BA.debugLine="Dim sms As PhoneSms";
_sms = new anywheresoftware.b4a.phone.Phone.PhoneSms();
 //BA.debugLineNum = 29;BA.debugLine="Dim mobile1 As String";
_mobile1 = "";
 //BA.debugLineNum = 30;BA.debugLine="Dim mobile2 As String";
_mobile2 = "";
 //BA.debugLineNum = 31;BA.debugLine="Dim intercept As SmsInterceptor";
_intercept = new anywheresoftware.b4a.phone.PhoneEvents.SMSInterceptor();
 //BA.debugLineNum = 32;BA.debugLine="Dim total As Int";
_total = 0;
 //BA.debugLineNum = 33;BA.debugLine="Dim timer1 As Timer";
_timer1 = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 35;BA.debugLine="End Sub";
return "";
}
public static String  _readtextreader() throws Exception{
anywheresoftware.b4a.objects.streams.File.TextReaderWrapper _textreader1 = null;
 //BA.debugLineNum = 374;BA.debugLine="Sub ReadTextReader";
 //BA.debugLineNum = 375;BA.debugLine="Dim TextReader1 As TextReader";
_textreader1 = new anywheresoftware.b4a.objects.streams.File.TextReaderWrapper();
 //BA.debugLineNum = 376;BA.debugLine="If Not (File.Exists(File.DirInternal, \"Text.txt\")";
if (anywheresoftware.b4a.keywords.Common.Not(anywheresoftware.b4a.keywords.Common.File.Exists(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"Text.txt"))) { 
 //BA.debugLineNum = 377;BA.debugLine="user=\"admin\"";
mostCurrent._user = "admin";
 //BA.debugLineNum = 378;BA.debugLine="mobile1=\"9545330808\"";
_mobile1 = "9545330808";
 //BA.debugLineNum = 379;BA.debugLine="mobile2=\"7798058282\"";
_mobile2 = "7798058282";
 //BA.debugLineNum = 381;BA.debugLine="WriteTextWriter";
_writetextwriter();
 };
 //BA.debugLineNum = 383;BA.debugLine="TextReader1.Initialize(File.OpenInput(File.Dir";
_textreader1.Initialize((java.io.InputStream)(anywheresoftware.b4a.keywords.Common.File.OpenInput(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"Text.txt").getObject()));
 //BA.debugLineNum = 387;BA.debugLine="user=TextReader1.ReadLine";
mostCurrent._user = _textreader1.ReadLine();
 //BA.debugLineNum = 388;BA.debugLine="mobile1 = TextReader1.ReadLine";
_mobile1 = _textreader1.ReadLine();
 //BA.debugLineNum = 389;BA.debugLine="mobile2 = TextReader1.ReadLine";
_mobile2 = _textreader1.ReadLine();
 //BA.debugLineNum = 390;BA.debugLine="txtUserSave.Text=user";
mostCurrent._txtusersave.setText((Object)(mostCurrent._user));
 //BA.debugLineNum = 391;BA.debugLine="txtMobile1.Text=mobile1";
mostCurrent._txtmobile1.setText((Object)(_mobile1));
 //BA.debugLineNum = 392;BA.debugLine="txtMobile2.Text =mobile2";
mostCurrent._txtmobile2.setText((Object)(_mobile2));
 //BA.debugLineNum = 410;BA.debugLine="TextReader1.Close";
_textreader1.Close();
 //BA.debugLineNum = 411;BA.debugLine="End Sub";
return "";
}
public static String  _timer1_tick() throws Exception{
String _str1 = "";
 //BA.debugLineNum = 198;BA.debugLine="Sub Timer1_tick";
 //BA.debugLineNum = 199;BA.debugLine="Dim str1 As String";
_str1 = "";
 //BA.debugLineNum = 200;BA.debugLine="str1= \"User ID:\" & user & CRLF & \"Fall Detected\"";
_str1 = "User ID:"+mostCurrent._user+anywheresoftware.b4a.keywords.Common.CRLF+"Fall Detected"+anywheresoftware.b4a.keywords.Common.CRLF+"Location: http://www.google.com/maps/place/"+mostCurrent._lbllatitude.getText()+","+mostCurrent._lbllongitude.getText();
 //BA.debugLineNum = 202;BA.debugLine="sms.Send(mobile1,str1)";
_sms.Send(_mobile1,_str1);
 //BA.debugLineNum = 203;BA.debugLine="sms.Send(mobile2,str1)";
_sms.Send(_mobile2,_str1);
 //BA.debugLineNum = 204;BA.debugLine="EditText1.Text=\"\"";
mostCurrent._edittext1.setText((Object)(""));
 //BA.debugLineNum = 205;BA.debugLine="timer1.Enabled=False";
_timer1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 206;BA.debugLine="End Sub";
return "";
}
public static String  _writetextwriter() throws Exception{
anywheresoftware.b4a.objects.streams.File.TextWriterWrapper _textwriter1 = null;
 //BA.debugLineNum = 360;BA.debugLine="Sub WriteTextWriter";
 //BA.debugLineNum = 361;BA.debugLine="Dim TextWriter1 As TextWriter";
_textwriter1 = new anywheresoftware.b4a.objects.streams.File.TextWriterWrapper();
 //BA.debugLineNum = 362;BA.debugLine="TextWriter1.Initialize(File.OpenOutput(File.Di";
_textwriter1.Initialize((java.io.OutputStream)(anywheresoftware.b4a.keywords.Common.File.OpenOutput(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"Text.txt",anywheresoftware.b4a.keywords.Common.False).getObject()));
 //BA.debugLineNum = 366;BA.debugLine="TextWriter1.WriteLine(txtUserSave.Text)";
_textwriter1.WriteLine(mostCurrent._txtusersave.getText());
 //BA.debugLineNum = 367;BA.debugLine="TextWriter1.WriteLine(txtMobile1.Text)";
_textwriter1.WriteLine(mostCurrent._txtmobile1.getText());
 //BA.debugLineNum = 368;BA.debugLine="TextWriter1.WriteLine(txtMobile2.Text)";
_textwriter1.WriteLine(mostCurrent._txtmobile2.getText());
 //BA.debugLineNum = 370;BA.debugLine="TextWriter1.Close";
_textwriter1.Close();
 //BA.debugLineNum = 371;BA.debugLine="ReadTextReader";
_readtextreader();
 //BA.debugLineNum = 372;BA.debugLine="End Sub";
return "";
}
}
