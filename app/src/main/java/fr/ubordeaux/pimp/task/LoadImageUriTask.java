package fr.ubordeaux.pimp.task;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import fr.ubordeaux.pimp.R;
import fr.ubordeaux.pimp.activity.MainActivity;
import fr.ubordeaux.pimp.fragments.EffectsFragment;
import fr.ubordeaux.pimp.image.Image;
import fr.ubordeaux.pimp.image.ImagePack;

/**
 * Load asynchronously a new Image from Uri with async task
 */
public class LoadImageUriTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<MainActivity> activityWeakReference; //MainActivity reference
    private Image image;
    private ImagePack imagePack;
    private Uri source;
    private boolean first;

    public LoadImageUriTask(MainActivity activity, Uri source) {
        this(activity, source, false);
    }

    public LoadImageUriTask(MainActivity activity, Uri source, boolean first) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.source = source;
        this.first = first;
    }

    //Work to do before heavy task
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) { //Prevent memory leaks
            return;
        }

        if (!first) {
            activity.hideMenu(); //Hide menu
            activity.hideEffectsList();
        }
        activity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE); //Show progressBar
    }

    //Heavy task to do
    @Override
    protected Void doInBackground(Void... voids) {
        MainActivity activity = activityWeakReference.get();
        while (!isCancelled()) { //Prevent cancelled task by task.cancel()
            if (activity == null || activity.isFinishing()) {
                return null;
            }
            try {
                image = new Image(source, activity); //load and create Image
                imagePack = new ImagePack(image, MainActivity.PREVIEWS_WIDTH, MainActivity.PREVIEWS_HEIGHT);
                EffectsFragment.createPreviews(imagePack, activity); //create previews
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
                return null;
            }

            return null;
        }

        return null;
    }


    //Update image when heavy task is finished
    @Override
    protected void onPostExecute(Void voids) {
        super.onPostExecute(voids);
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (!first) {
            activity.showMenu();
            activity.showEffectsList();
        }
        //***Linked to main activity ***/
        activity.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        activity.setImagePack(imagePack);
        activity.updateIv();
        activity.showPreviews();

    }

    @Override
    protected void onCancelled(Void voids) { //Something wrong happenend return to firstActivity
        Toast.makeText(activityWeakReference.get(), "Something went wrong, file may be corrupted", Toast.LENGTH_LONG).show();
        activityWeakReference.get().finish();
    }
}