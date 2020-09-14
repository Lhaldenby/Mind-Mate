package com.notts.MindMate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditAppsActivity extends AppCompatActivity {

    private List<AppList> installedApps;
    private AppAdapter installedAppAdapter;
    ListView userInstalledApps;
    private List<String> appList = new ArrayList<String>();
    private final FileHandling fileHandling = new FileHandling(this);
    private final String filename = "TrackedApps.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_apps);


        String data = "";


        try {
            data = fileHandling.readFile(this, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Approved Apps:");
        if (data != null && data != "") {
            final String[] dataArray = data.split(";");

            for (int i=0; i < dataArray.length; i++){
                appList.add(dataArray[i]);
                System.out.println(dataArray[i]);
            }

        }

        userInstalledApps = (ListView) findViewById(R.id.installed_app_list);

        installedApps = getInstalledApps();
        installedAppAdapter = new AppAdapter(EditAppsActivity.this, installedApps);
        userInstalledApps.setAdapter(installedAppAdapter);

    }

    private List<AppList> getInstalledApps() {
        PackageManager pm = getPackageManager();
        List<AppList> apps = new ArrayList<AppList>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        //List<PackageInfo> packs = getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (int i = 0; i < packs.size(); i++) {

            PackageInfo p = packs.get(i);

            if ((!isSystemPackage(p))) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
                String packages = p.applicationInfo.packageName;
                Boolean checked = false;
                if (appList.contains(packages)){
                    checked = true;
                }
                apps.add(new AppList(appName, icon, packages, checked));
            }

        }
        return apps;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {

        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

    }

    public class AppAdapter extends BaseAdapter {

        public LayoutInflater layoutInflater;
        public List<AppList> listStorage;

        public AppAdapter(Context context, List<AppList> customizedListView) {
            layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listStorage = customizedListView;
        }

        @Override
        public int getCount() {
            return listStorage.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder listViewHolder;
            if(convertView == null){
                listViewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.installed_app_list, parent, false);

                listViewHolder.textInListView = (TextView)convertView.findViewById(R.id.list_app_name);
                listViewHolder.imageInListView = (ImageView)convertView.findViewById(R.id.app_icon);
                listViewHolder.packageInListView=(TextView)convertView.findViewById(R.id.app_package);
                listViewHolder.appSwitchInListView=(Switch)convertView.findViewById(R.id.appSwitch);
                convertView.setTag(listViewHolder);
            }else{
                listViewHolder = (ViewHolder)convertView.getTag();
            }
            listViewHolder.textInListView.setText(listStorage.get(position).getName());
            listViewHolder.imageInListView.setImageDrawable(listStorage.get(position).getIcon());
            listViewHolder.packageInListView.setText(listStorage.get(position).getPackages());
            listViewHolder.appSwitchInListView.setChecked(listStorage.get(position).getChecked());
            listViewHolder.appSwitchInListView.setOnClickListener(new CompoundButton.OnClickListener(){

                @Override
                public void onClick(View v) {

                    if (listStorage.get(position).getChecked()){
                        listStorage.get(position).setChecked(false);
                        appList.remove(listStorage.get(position).getPackages());
                    } else {
                        listStorage.get(position).setChecked(true);
                        appList.add(listStorage.get(position).getPackages());
                    }

                    try {
                        fileHandling.clearFile(EditAppsActivity.this,filename);
                        String d = fileHandling.readFile(EditAppsActivity.this, filename);
                        System.out.println("data: "+d);
                        System.out.println("Approved Apps:");
                        for (int i=0; i < appList.size(); i++) {
                            System.out.println(appList.get(i));
                            fileHandling.writeFile(EditAppsActivity.this, filename, appList.get(i) + ";");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });

            return convertView;
        }

        class ViewHolder{
            TextView textInListView;
            ImageView imageInListView;
            TextView packageInListView;
            Switch appSwitchInListView;
        }
    }

    public class AppList {
        private String name;
        Drawable icon;
        private String packages;
        private Boolean checked;
        public AppList(String name, Drawable icon, String packages, Boolean checked) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
            this.checked = checked;
        }
        public String getName() {
            return name;
        }
        public Drawable getIcon() {
            return icon;
        }
        public String getPackages() {
            return packages;
        }
        public Boolean getChecked() { return checked; }
        public void setChecked(Boolean checked) {this.checked = checked;}

    }

    public void back(View view){
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

}