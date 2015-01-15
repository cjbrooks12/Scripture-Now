package com.caseybrooks.scripturememory.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.HelpFragment;
import com.caseybrooks.scripturememory.fragments.ImportVersesFragment;
import com.caseybrooks.scripturememory.fragments.NavigationDrawerFragment;
import com.caseybrooks.scripturememory.fragments.SettingsFragment;
import com.caseybrooks.scripturememory.fragments.TopicalBibleFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class MainActivity extends ActionBarActivity implements NavigationCallbacks {
//Data members
//------------------------------------------------------------------------------		
    Toolbar tb;
    Context context;

//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        context = this;

        int theme = MetaSettings.getAppTheme(context);
		if(theme == 0) setTheme(R.style.Theme_BaseLight);
		else setTheme(R.style.Theme_BaseDark);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // Set up the drawer.
        tb = (Toolbar) findViewById(R.id.activity_toolbar);
        setSupportActionBar(tb);

        NavigationDrawerFragment mNavigationDrawerFragment = new NavigationDrawerFragment();
        mNavigationDrawerFragment.setUp(this, tb,  findViewById(R.id.navigation_drawer_container),
                (DrawerLayout) findViewById(R.id.drawer_layout));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                //put fragment in navigation drawer
                .add(R.id.navigation_drawer_container, mNavigationDrawerFragment)
                .commit();

                //put DashboardFragment in main content
        fragmentManager.beginTransaction()
                .add(R.id.mainFragmentContainer, new DashboardFragment())
                .commit();

		getOverflowMenu();
		showFirstTime();
		showPrompt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MAIN ACTIVITY", "on resume() called");
        receiveImplicitIntent();
    }

    //TODO: Setup app for first time opened. Will need to remove all current "first" time methods and make brand new one
	private void showFirstTime() {
		boolean firstTime = MetaSettings.getFirstTime(context);
		//If this is the first time opening the app, load a set of verses to memorize
		if(firstTime) {
            try {
                //move all verses from the old database to the new one
                VersesDatabase database = new VersesDatabase(context).open();
                database.migrate();
                database.close();

                // initial verse packs are in /res/raw/, but we need them on the sdcard. We must copy
                // all files from raw to the sdcard the first time we open the app
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";

                    File folder = new File(path);

                    if(!folder.exists()) {
                        folder.mkdirs();
                    }
                    if(folder.exists()) {
                        Source[] domSource = new StreamSource[] {
                                new StreamSource(getResources().openRawResource(R.raw.gods_holiness)),
                                new StreamSource(getResources().openRawResource(R.raw.mans_depravity)),
                                new StreamSource(getResources().openRawResource(R.raw.share_the_gospel)),
                                new StreamSource(getResources().openRawResource(R.raw.the_command_of_christ)),
                                new StreamSource(getResources().openRawResource(R.raw.the_person_of_christ)),
                                new StreamSource(getResources().openRawResource(R.raw.the_work_of_christ)),
                                new StreamSource(getResources().openRawResource(R.raw.roman_road)),
                                new StreamSource(getResources().openRawResource(R.raw.original_scripturememory_verses)),
                                new StreamSource(getResources().openRawResource(R.raw.topical_memory_system)),
                                new StreamSource(getResources().openRawResource(R.raw.help_with_anger)),
                                new StreamSource(getResources().openRawResource(R.raw.help_with_despair)),
                                new StreamSource(getResources().openRawResource(R.raw.help_with_fear))
                        };

                        File[] outputStream = new File[]{
                                new File(path, "gods_holiness.xml"),
                                new File(path, "mans_depravity.xml"),
                                new File(path, "share_the_gospel.xml"),
                                new File(path, "the_command_of_christ.xml"),
                                new File(path, "the_person_of_christ.xml"),
                                new File(path, "the_work_of_christ.xml"),
                                new File(path, "roman_road.xml"),
                                new File(path, "original_scripturememory_verses.xml"),
                                new File(path, "topical_memory_system.xml"),
                                new File(path, "help_with_anger.xml"),
                                new File(path, "help_with_despair.xml"),
                                new File(path, "help_with_fear.xml")
                        };

                        TransformerFactory factory = TransformerFactory.newInstance();
                        Transformer transformer = factory.newTransformer();

                        for(int i = 0; i < outputStream.length; i++) {
                            transformer.transform(domSource[i], new StreamResult(outputStream[i]));
                        }
                    }

                    //migrate the old backup format to the new one so that old backups are not lost
                    File oldBackup = new File(path, "backup.csv");
                    ArrayList<Passage> passages = new ArrayList<Passage>();

                    BufferedReader buffer = new BufferedReader(new FileReader(oldBackup));
                    String line = "";

                    while ((line = buffer.readLine()) != null) {
                        String[] str = line.split("\t");

                        try {
                            Passage passage = new Passage(str[1]);
                            passage.setText(str[2]);
                            if (str[3].equals("memorized"))
                                passage.getMetadata().putInt(DefaultMetaData.STATE, 4);
                            else
                                passage.getMetadata().putInt(DefaultMetaData.STATE, 1);
                            passage.addTag("Previous Backup");
                            passages.add(passage);
                        }
                        catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    buffer.close();

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();

                    Document doc = builder.newDocument();
                    Element root = doc.createElement("backup");
                    doc.appendChild(root);

                    Element tags = doc.createElement("tags");
                    root.appendChild(tags);

                    Element verses = doc.createElement("verses");
                    root.appendChild(verses);

                    for(Passage passage : passages) {
                        Element passageElement = doc.createElement("passage");
                        verses.appendChild(passageElement);

                        passageElement.setAttribute("state", Integer.toString(passage.getMetadata().getInt(DefaultMetaData.STATE)));
                        passageElement.setAttribute("time_created", Long.toString(passage.getMetadata().getLong(DefaultMetaData.TIME_CREATED)));
                        passageElement.setAttribute("time_modified", Long.toString(passage.getMetadata().getLong(DefaultMetaData.TIME_MODIFIED)));

                        Element r = doc.createElement("R");
                        r.appendChild(doc.createTextNode(passage.getReference().toString()));
                        passageElement.appendChild(r);

                        Element q = doc.createElement("Q");
                        q.appendChild(doc.createTextNode(passage.getVersion().getName()));
                        passageElement.appendChild(q);

                        Element t = doc.createElement("T");
                        passageElement.appendChild(t);
                        for(String string : passage.getTags()) {
                            Element tagItem = doc.createElement("item");
                            tagItem.appendChild(doc.createTextNode(string));
                            t.appendChild(tagItem);
                        }

                        Element p = doc.createElement("P");
                        p.appendChild(doc.createTextNode(passage.getText()));
                        passageElement.appendChild(p);
                    }

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File(path, "backup.xml"));
                    transformer.transform(source, result);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //we have finished our first time setup, do not run this code again
                MetaSettings.putFirstTime(context, false);
            }
		}
	}

	private void showPrompt() {
		if(MetaSettings.getPromptOnStart(context) == 3) {
            MetaSettings.putPromptOnStart(context, 4);

            final View view = LayoutInflater.from(context).inflate(R.layout.popup_rate_app, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);

            final AlertDialog dialog = builder.create();

            view.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String shareMessage = getResources().getString(R.string.share_message);
                    Intent intent = new Intent();
                    intent.setType("text/plain");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Try Scripture Memory Notifications for Android");
                    intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(intent, "Share To..."));
                    dialog.dismiss();
                }
            });

            view.findViewById(R.id.rate_it_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
					final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

					if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
						startActivity(rateAppIntent);
					}
                    dialog.dismiss();
                }
            });

            view.findViewById(R.id.no_thanks_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "You can always do it again from settings", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

            view.findViewById(R.id.remind_me_later_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MetaSettings.putPromptOnStart(context, 0);
                    dialog.dismiss();
                }
            });

            dialog.show();
		}
        else if(MetaSettings.getPromptOnStart(context) < 3){
            MetaSettings.putPromptOnStart(context, MetaSettings.getPromptOnStart(context) + 1);
        }
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void receiveImplicitIntent() {
        Bundle extras = getIntent().getExtras();
		if(extras != null && extras.containsKey(Intent.EXTRA_TEXT)) {
            DashboardFragment dashboard = new DashboardFragment();
            dashboard.setArguments(extras);
            setFragment(dashboard);
        }
	}

//ActionBar
//------------------------------------------------------------------------------
	//Forces three dot overflow on devices with hardware menu button.
	//Some might consider this a hack...
	private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}

//Everything to do with new NavDrawerFragment
//------------------------------------------------------------------------------

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.push_up_in, 0)
                .replace(R.id.mainFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationDrawerFragment.NavListItem item) {
        switch(item.groupPosition) {
            case 0:
                toDashboard();
                break;
            case 1:
                if(item.childPosition == 0) toTopicalBible();
                else toImportVerses();
                break;
            case 2:
                toVerseList(VerseListFragment.STATE, item.id);
                break;
            case 3:
                toVerseList(VerseListFragment.TAGS, item.id);
                break;
            case 4:
                toSettings();
                break;
            case 5:
                toHelp();
                break;
            default:
        }
    }

    @Override
    public void toVerseDetail(int id) {

    }

    @Override
    public void toVerseEdit(int id) {
        Intent intent = new Intent(this, DetailActivity.class);
        Bundle extras = new Bundle();
        extras.putInt("KEY_ID", id);
        extras.putInt("KEY_LIST_TYPE", MetaSettings.getActiveList(context).first);
        extras.putInt("KEY_LIST_ID", MetaSettings.getActiveList(context).second);
        extras.putInt("FRAGMENT", 0);


        intent.putExtras(extras);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, 0);
    }

    @Override
    public void toVerseList(int listType, int id) {
        Fragment fragment = VerseListFragment.newInstance(listType, id);
        setFragment(fragment);
    }

    @Override
    public void toDashboard() {
        Fragment fragment = new DashboardFragment();
        setFragment(fragment);
    }

    @Override
    public void toTopicalBible() {
        Fragment fragment = TopicalBibleFragment.newInstance();
        setFragment(fragment);
    }

    @Override
    public void toImportVerses() {
        Fragment fragment = ImportVersesFragment.newInstance();
        setFragment(fragment);
    }

    @Override
    public void toSettings() {
        Fragment fragment = new SettingsFragment();
        setFragment(fragment);
    }

    @Override
    public void toHelp() {
        Fragment fragment = new HelpFragment();
        setFragment(fragment);
    }
}