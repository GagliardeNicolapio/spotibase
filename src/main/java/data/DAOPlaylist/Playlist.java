package data.DAOPlaylist;
import data.DAOCanzone.Canzone;
import data.DAOUtente.Utente;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
/**Questa classe rappresenta il bean della playlist
 *
 */
public class Playlist {
	private String titolo;
	private String note;
	//private double durata;
	//private LocalDate dataCreazione;
	private String username;
	private Utente utente;
	private List<Canzone> canzoni;

	@Override
	@Generated
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Playlist playlist = (Playlist) o;
		return playlist.getTitolo().equals(this.titolo) && playlist.getUsername().equals(this.username);
	}



	@Override
	@Generated
	public String toString() {
		return "Playlist{" +
				"titolo='" + titolo + '\'' +
				", note='" + note + '\'' +
				", username='" + username + '\'' +
				", utente=" + utente +
				", canzoni=" + canzoni +
				'}';
	}

	public Utente getUtente() {
		return utente;
	}

	public void setUtente(Utente utente) {
		this.utente = utente;
	}

	public List<Canzone> getCanzoni() {
		return canzoni;
	}

	public void setCanzoni(List<Canzone> canzoni) {
		this.canzoni = canzoni;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public String getTitolo() {
		return titolo;
	}
	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

	/*public double getDurata() {
		return durata;
	}
	public void setDurata(double durata) {
		this.durata = durata;
	}
	public LocalDate getDataCreazione() {
		return dataCreazione;
	}
	public void setDataCreazione(LocalDate dataCrezione) {
		this.dataCreazione = dataCrezione;
	}*/



}
