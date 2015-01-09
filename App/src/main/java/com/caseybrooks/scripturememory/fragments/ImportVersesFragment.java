package com.caseybrooks.scripturememory.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.enumeration.Version;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VerseDB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.ArrayList;

public class ImportVersesFragment extends Fragment {
    Context context;
    ListView lv;
    FileAdapter adapter;

//    NavigationCallbacks mCallbacks;

    public static Fragment newInstance() {
        Fragment fragment = new ImportVersesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main layout for this fragment
        lv = new ListView(getActivity());

        return lv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((ActionBarActivity) context).getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(context.getResources().getColor(R.color.memorized));
        ab.setBackgroundDrawable(colorDrawable);
        ab.setTitle("Import");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        populateList();
    }

    private void populateList() {
        String path = Environment.getExternalStorageDirectory().toString()+"/scripturememory";
        File f = new File(path);
        File files[] = f.listFiles();
        ArrayList<File> goodFiles = new ArrayList<File>();

        for(File file : files) {
            try {
                Document doc = Jsoup.parse(file, null);

                if (doc.select("verses").size() > 0) {

                    goodFiles.add(file);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        adapter = new FileAdapter(goodFiles);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder vh = (ViewHolder) view.getTag();

                if(vh != null) {
                    new SaveVerses().execute(vh.file);
                }
            }
        });
    }

    private class SaveVerses extends AsyncTask<File, Void, Void> {

        View view;
        AlertDialog dialog;
        ArrayList<Passage> verses;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            verses = new ArrayList<Passage>();

            view = LayoutInflater.from(context).inflate(R.layout.popup_import_verses, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);

            dialog = builder.create();

            view.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            view.findViewById(R.id.description).setVisibility(View.GONE);
            view.findViewById(R.id.import_button).setVisibility(View.GONE);

            dialog.show();
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            view.findViewById(R.id.progress).setVisibility(View.GONE);
            view.findViewById(R.id.description).setVisibility(View.VISIBLE);
            view.findViewById(R.id.import_button).setVisibility(View.VISIBLE);

            TextView addVerseButton = (TextView) view.findViewById(R.id.import_button);
            addVerseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VerseDB db = new VerseDB(context).open();
                    for(Passage passage : verses) {
                        db.insertVerse(passage);
                    }
                    dialog.cancel();
                }
            });
        }

        @Override
        protected Void doInBackground(File... params) {
            try {
                Document doc = Jsoup.parse(params[0], null);

                TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        cancel(true);
                    }
                });

                if(doc.select("verses").size() > 0) {
                    for(Element element : doc.select("passage")) {
                        if(isCancelled()) break;
                        Passage passage = new Passage(element.select("R").text());
                        passage.setVersion(Version.parseVersion(element.select("Q").text()));
                        passage.addTag(element.select("T").text());
                        passage.setText(element.select("P").text());
                        passage.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);

                        verses.add(passage);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "There was an error importing this verse pack", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

            return null;
        }



    }

    private class ViewHolder {
        TextView title;
        TextView count;
        TextView source;

        File file;
        Context context;

        ViewHolder(Context context, View inflater) {
            this.context = context;

            title = (TextView) inflater.findViewById(R.id.fileName);
            count = (TextView) inflater.findViewById(R.id.fileVerseCount);
            source = (TextView) inflater.findViewById(R.id.fileSource);
        }

        private void initialize(File file) {
            this.file = file;

            try {
                Document doc = Jsoup.parse(file, null);

                if(doc.select("verses").size() > 0) {

                    if(doc.select("verses").hasAttr("name")) {
                        title.setText(doc.select("verses").attr("name"));
                    }
                    else {
                        title.setText(file.getName());
                    }

                    count.setText(doc.select("passage").size() + " verses");

                    if( file.getName().equalsIgnoreCase("gods_holiness.xml") ||
                        file.getName().equalsIgnoreCase("mans_depravity.xml") ||
                        file.getName().equalsIgnoreCase("share_the_gospel.xml") ||
                        file.getName().equalsIgnoreCase("the_command_of_christ.xml") ||
                        file.getName().equalsIgnoreCase("the_person_of_christ.xml") ||
                        file.getName().equalsIgnoreCase("the_work_of_christ.xml") ||
                        file.getName().equalsIgnoreCase("roman_road.xml")) {
                        source.setText("Included with app");
                    }
                    else {
                        source.setText("From external source");
                    }

                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FileAdapter extends BaseAdapter {
        ArrayList<File> files;


        public FileAdapter(ArrayList<File> files) {
            this.files = files;
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public File getItem(int position) {
            return files.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.list_import_verses, parent, false);
                vh = new ViewHolder(context, view);
                view.setTag(vh);
            }
            else {
                vh = (ViewHolder) view.getTag();
            }

            vh.initialize(files.get(position));
            view.setTag(vh);

            return view;
        }
    }



//Host Activity Interface
//------------------------------------------------------------------------------
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mCallbacks = (NavigationCallbacks) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Activity must implement NavigationCallbacks.");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mCallbacks = null;
//    }
}
