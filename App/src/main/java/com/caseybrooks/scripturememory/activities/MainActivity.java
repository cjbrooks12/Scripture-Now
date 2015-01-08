package com.caseybrooks.scripturememory.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
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

        new TranslateRememberMeFiles().execute();
		getOverflowMenu();
		showFirstTime();
		showPrompt();

		receiveImplicitIntent();
    }

    //TODO: Setup app for first time opened. Will need to remove all current "first" time methods and make brand new one
	private void showFirstTime() {
		boolean firstTime = MetaSettings.getFirstTime(context);
		//If this is the first time opening the app, load a set of verses to memorize
		if(firstTime) {
            try {
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
                                new StreamSource(getResources().openRawResource(R.raw.roman_road))
                        };

                        File[] outputStream = new File[]{
                                new File(path, "gods_holiness.xml"),
                                new File(path, "mans_depravity.xml"),
                                new File(path, "share_the_gospel.xml"),
                                new File(path, "the_command_of_christ.xml"),
                                new File(path, "the_person_of_christ.xml"),
                                new File(path, "the_work_of_christ.xml"),
                                new File(path, "roman_road.xml")
                        };

                        TransformerFactory factory = TransformerFactory.newInstance();
                        Transformer transformer = factory.newTransformer();

                        for(int i = 0; i < outputStream.length; i++) {
                            transformer.transform(domSource[i], new StreamResult(outputStream[i]));
                        }
                    }
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

    private class VerseStrings {
        String ref;
        String version;
        String tags;
        String text;
    }

    private class TranslateRememberMeFiles extends AsyncTask<Void, Void, Void> {
        ArrayList<VerseStrings> verses = new ArrayList<VerseStrings>();

        @Override
        protected Void doInBackground(Void... voids) {
            //pathA = Scripture Now's external directory
            //pathB = RememberMe's external directory

            String pathA = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";
            String pathB = Environment.getExternalStorageDirectory().getPath() + "/RememberMe";

            File folderA = new File(pathA);
            File folderB = new File(pathB);

            if(folderB.exists() && folderB.isDirectory() && folderB.listFiles().length > 0) {
                //the RememberMe folder exists and has files in it, which must be to imported

                for(File file : folderB.listFiles()) {
                    if(file.getName().equals("books_of_bible.txt")) continue;
                    if(!file.getName().contains(".txt")) continue;

                    Log.e("PARSING FILE:", file.getName());
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(file));

                        String line;
                        VerseStrings passage = null;

                        while (true) {
                            line = in.readLine();
                            if (line == null) break;
                            if (line.length() == 0) continue;

                            String lineType = line.substring(0, 2).toLowerCase().trim();

                            if (lineType.equals("r:")) {
                                if (passage == null) {
                                    passage = new VerseStrings();
                                } else {
                                    if(passage.ref == null || passage.version == null || passage.tags == null || passage.text == null){

                                    }
                                    else {
                                        verses.add(passage);
                                    }

                                    passage = new VerseStrings();
                                }
                                passage.ref = line.substring(2).trim();
                            } else if (lineType.equals("q:")) {
                                passage.version = line.substring(2).trim();
                            } else if (lineType.equals("t:")) {
                                passage.tags = line.substring(2).trim();
                            } else if (lineType.equals("p:")) {
                                passage.text = line.substring(2).trim();
                            } else {
                                passage.text += " " + line.substring(2).trim();
                            }
                        }

                        //print verses to an XML file
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();

                        Document doc = builder.newDocument();
                        Element root = doc.createElement("verses");
                        root.setAttribute("name", file.getName().replaceAll("_", " ").replaceAll(".txt", ""));
                        doc.appendChild(root);

                        for(int i = 0; i < verses.size(); i++) {
                            Element passageElement = doc.createElement("passage");
                            root.appendChild(passageElement);

                            Element r = doc.createElement("R");
                            r.appendChild(doc.createTextNode(verses.get(i).ref));
                            passageElement.appendChild(r);

                            Element q = doc.createElement("Q");
                            q.appendChild(doc.createTextNode(verses.get(i).version));
                            passageElement.appendChild(q);

                            //TODO: write all tags to file
                            Element t = doc.createElement("T");
                            t.appendChild(doc.createTextNode(verses.get(i).tags));
                            passageElement.appendChild(t);

                            Element p = doc.createElement("P");
                            p.appendChild(doc.createTextNode(verses.get(i).text));
                            passageElement.appendChild(p);
                        }

                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(new File(folderA, file.getName().replaceAll(".txt", ".xml")));
                        transformer.transform(source, result);
                    }
                    catch(FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                    }
                    catch(IOException ioe) {
                        ioe.printStackTrace();
                    }
                    catch (ParserConfigurationException pce) {
                        pce.printStackTrace();
                    }
                    catch (TransformerConfigurationException tce) {
                        tce.printStackTrace();
                    }
                    catch (TransformerException te) {
                        te.printStackTrace();
                    }
                }
            }



            return null;
        }
    }

	private void showPrompt() {
		if(MetaSettings.getPromptOnStart(context) == 3) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(true);
			builder.setTitle("Love Scripture Memory?");
			builder.setMessage("Help support this app by rating it on the Google Play Store, or share it with your friends!");
			builder.setPositiveButton("Rate it", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
					final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

					if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
						startActivity(rateAppIntent);
					}
				}
			});
			builder.setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					Toast.makeText(context, "You can always do it again from settings", Toast.LENGTH_LONG).show();
				}
			});
			builder.setNeutralButton("Share", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {

					String shareMessage = getResources().getString(R.string.share_message);
					Intent intent = new Intent();
					intent.setType("text/plain");
					intent.setAction(Intent.ACTION_SEND);
					intent.putExtra(Intent.EXTRA_SUBJECT, "Try Scripture Memory Notifications for Android");
					intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
					startActivity(Intent.createChooser(intent, "Share To..."));

				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Toast.makeText(context, "You can always do this later from settings", Toast.LENGTH_LONG).show();
				}
			});

            MetaSettings.putPromptOnStart(context, 4);
		    AlertDialog dialog = builder.create();
			dialog.show();
		}
        else if(MetaSettings.getPromptOnStart(context) < 3){
            MetaSettings.putPromptOnStart(context, MetaSettings.getPromptOnStart(context) + 1);
        }
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
        extras.putInt("KEY_LisT_ID", MetaSettings.getActiveList(context).second);

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