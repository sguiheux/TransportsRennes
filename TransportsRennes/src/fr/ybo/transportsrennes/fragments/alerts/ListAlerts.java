package fr.ybo.transportsrennes.fragments.alerts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import fr.ybo.transportsrennes.R;
import fr.ybo.transportsrennes.activity.alerts.DetailAlert;
import fr.ybo.transportsrennes.adapters.alerts.AlertAdapter;
import fr.ybo.transportsrennes.database.modele.Ligne;
import fr.ybo.transportsrennes.keolis.Keolis;
import fr.ybo.transportsrennes.keolis.modele.bus.Alert;
import fr.ybo.transportsrennes.util.ErreurReseau;

public class ListAlerts extends ListFragment {

	/**
	 * Permet d'accéder aux apis keolis.
	 */
	private final Keolis keolis = Keolis.getInstance();

	private final List<Alert> alerts = Collections.synchronizedList(new ArrayList<Alert>(50));

	private Ligne ligne = null;

	public void setLigne(Ligne ligne) {
		this.ligne = ligne;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Serializable alert = (Serializable) l.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), DetailAlert.class);
		intent.putExtra("alert", alert);
		startActivity(intent);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ListView lv = getListView();
		lv.setFastScrollEnabled(true);
		lv.setTextFilterEnabled(true);
		setListAdapter(new AlertAdapter(getActivity(), alerts));
		Alert alertChargement = new Alert();
		alertChargement.title = getString(R.string.dialogRequeteAlerts);
		alerts.add(alertChargement);
		new AsyncTask<Void, Void, Void>() {

			private boolean erreurReseau = false;

			private List<Alert> alertsTmp = new ArrayList<Alert>();

			@Override
			protected Void doInBackground(Void... params) {
				try {
					for (Alert alerte : keolis.getAlerts()) {
						while (alerte.lines.size() > 1) {
							Alert newAlerte = new Alert(alerte);
							newAlerte.lines.add(alerte.lines.remove(0));
							if (ligne != null) {
								if (ligne.nomCourt.equals(newAlerte.lines.get(0))) {
									alertsTmp.add(newAlerte);
								}
							} else {
								alertsTmp.add(newAlerte);
							}
						}
						if (ligne != null) {
							if (ligne.nomCourt.equals(alerte.lines.get(0))) {
								alertsTmp.add(alerte);
							}
						} else {
							alertsTmp.add(alerte);
						}
					}
				} catch (ErreurReseau e) {
					erreurReseau = true;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (erreurReseau) {
					Toast.makeText(getActivity(), getString(R.string.erreurReseau), Toast.LENGTH_LONG).show();
				} else {
					alerts.clear();
					alerts.addAll(alertsTmp);
					((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				}
				super.onPostExecute(result);
			}

		}.execute((Void) null);
	}

}