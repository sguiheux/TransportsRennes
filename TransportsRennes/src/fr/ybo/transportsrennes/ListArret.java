package fr.ybo.transportsrennes;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import fr.ybo.transportsrennes.adapters.ArretAdapter;
import fr.ybo.transportsrennes.keolis.gtfs.database.DataBaseException;
import fr.ybo.transportsrennes.keolis.gtfs.modele.ArretFavori;
import fr.ybo.transportsrennes.keolis.gtfs.modele.Route;
import fr.ybo.transportsrennes.util.LogYbo;

import java.lang.reflect.Field;
import java.util.Collections;

/**
 * Liste des arrêts d'une ligne de bus.
 *
 * @author ybonnel
 */
public class ListArret extends ListActivity {

	private final static Class<?> classDrawable = R.drawable.class;

	private final static LogYbo LOG_YBO = new LogYbo(ListArret.class);

	private Route myRoute;

	private Cursor currentCursor;

	private void closeCurrentCursor() {
		if (currentCursor != null && !currentCursor.isClosed()) {
			currentCursor.close();
		}
	}

	private void ajoutFavori(final Cursor cursor) throws DataBaseException {
		final ArretFavori arretFavori = new ArretFavori();
		arretFavori.setStopId(cursor.getString(cursor.getColumnIndex("_id")));
		arretFavori.setNomArret(cursor.getString(cursor.getColumnIndex("arretName")));
		arretFavori.setDirection(cursor.getString(cursor.getColumnIndex("direction")));
		arretFavori.setRouteId(myRoute.getId());
		arretFavori.setRouteNomCourt(myRoute.getNomCourt());
		arretFavori.setRouteNomLong(myRoute.getNomLong());
		LOG_YBO.debug("Ajout du favori " + arretFavori.getStopId());
		TransportsRennesApplication.getDataBaseHelper().insert(arretFavori);
	}

	private void construireCursor() {
		closeCurrentCursor();
		StringBuilder requete = new StringBuilder();
		requete.append("select Arret.id as _id, Arret.nom as arretName,");
		requete.append(" ArretRoute.direction as direction ");
		requete.append("from ArretRoute, Arret ");
		requete.append("where");
		requete.append(" ArretRoute.routeId = :routeId");
		requete.append(" and ArretRoute.arretId = Arret.id");
		requete.append(" order by ArretRoute.direction, Arret.nom;");
		LOG_YBO.debug("Exécution de la requete permettant de récupérer les arrêts avec le temps avant le prochain");
		LOG_YBO.debug(requete.toString());
		currentCursor =
				TransportsRennesApplication.getDataBaseHelper().executeSelectQuery(requete.toString(), Collections.singletonList(myRoute.getId()));
		LOG_YBO.debug("Exécution de la requete permettant de récupérer les arrêts terminée : " + currentCursor.getCount());
	}

	private void construireListe() {
		construireCursor();
		setListAdapter(new ArretAdapter(getApplicationContext(), currentCursor, myRoute));
		final ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
				final ArretAdapter arretAdapter = (ArretAdapter) ((ListView) adapterView).getAdapter();
				final Cursor cursor = (Cursor) arretAdapter.getItem(position);
				final Intent intent = new Intent(ListArret.this, DetailArret.class);
				intent.putExtra("idArret", cursor.getString(cursor.getColumnIndex("_id")));
				intent.putExtra("nomArret", cursor.getString(cursor.getColumnIndex("arretName")));
				intent.putExtra("direction", cursor.getString(cursor.getColumnIndex("direction")));
				intent.putExtra("route", myRoute);
				startActivity(intent);
			}
		});
		lv.setTextFilterEnabled(true);
		registerForContextMenu(lv);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.ajoutFavori:
				ajoutFavori((Cursor) getListAdapter().getItem(info.position));
				return true;
			case R.id.supprimerFavori:
				supprimeFavori((Cursor) getListAdapter().getItem(info.position));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listearrets);
		myRoute = (Route) getIntent().getExtras().getSerializable("route");
		LinearLayout conteneur = (LinearLayout) findViewById(R.id.conteneurImage);
		TextView nomLong = (TextView) findViewById(R.id.nomLong);
		nomLong.setText(myRoute.getNomLongFormate());
		try {
			Field fieldIcon = classDrawable.getDeclaredField("i" + myRoute.getNomCourt().toLowerCase());
			int ressourceImg = fieldIcon.getInt(null);
			ImageView imgView = new ImageView(getApplicationContext());
			imgView.setImageResource(ressourceImg);
			conteneur.addView(imgView);
		} catch (NoSuchFieldException e) {
			TextView textView = new TextView(getApplicationContext());
			textView.setTextSize(16);
			textView.setText(myRoute.getNomCourt());
			conteneur.addView(textView);
		} catch (IllegalAccessException e) {
			TextView textView = new TextView(getApplicationContext());
			textView.setTextSize(16);
			textView.setText(myRoute.getNomCourt());
			conteneur.addView(textView);
		}
		construireListe();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
			String idArret = cursor.getString(cursor.getColumnIndex("_id"));
			String nomArret = cursor.getString(cursor.getColumnIndex("arretName"));
			ArretFavori arretFavori = new ArretFavori();
			arretFavori.setStopId(idArret);
			arretFavori = TransportsRennesApplication.getDataBaseHelper().selectSingle(arretFavori);
			menu.setHeaderTitle(nomArret);
			menu.add(Menu.NONE, arretFavori == null ? R.id.ajoutFavori : R.id.supprimerFavori, 0,
					arretFavori == null ? "Ajouter aux favoris" : "Supprimer des favoris");
		}
	}

	@Override
	protected void onDestroy() {
		closeCurrentCursor();
		super.onPause();
	}

	private void supprimeFavori(final Cursor cursor) throws DataBaseException {
		final ArretFavori arretFavori = new ArretFavori();
		arretFavori.setStopId(cursor.getString(cursor.getColumnIndex("_id")));
		LOG_YBO.debug("Suppression du favori " + arretFavori.getStopId());
		TransportsRennesApplication.getDataBaseHelper().delete(arretFavori);
	}
}