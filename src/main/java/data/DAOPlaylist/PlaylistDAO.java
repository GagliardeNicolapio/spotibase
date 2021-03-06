package data.DAOPlaylist;

import data.DAOCanzone.CanzoneAPI;
import data.DAOCanzone.CanzoneMapper;
import data.DAOUtente.UtenteAPI;
import data.DAOUtente.UtenteDAO;
import data.Exceptions.OggettoGiaPresenteException;
import data.Exceptions.OggettoNonCancellatoException;
import data.Exceptions.OggettoNonInseritoException;
import data.Exceptions.OggettoNonTrovatoException;
import data.utils.SingletonJDBC;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@interface Generated {
}

/** Questa classe contine i metodi per la gestione della playlsit specificati nel ODD
 *
 * @version 1.0
 * @see PlaylistAPI interfaccia della classe
 */
public class PlaylistDAO implements PlaylistAPI {


    //metodi documentati per IS-----------------------------------------------------------------------------------------

    private Connection connection;
    //ciao

    public PlaylistDAO(){
        this.connection = SingletonJDBC.getConnection();
    }
    public PlaylistDAO(Connection connection){
        this.connection = connection;
    }

    /**Questo metodo preleva una playlist dal database
     * <p><b>pre: </b>chiave != null, chiave.contains(;) == true, il titolo della playlist deve essere presente anche l'username != null</p>
     * @param chiave concatenazione del titolo della plyalist con la username. Esempio: "playRock;pluto"
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando la chiave è null o non valida
     * @throws OggettoNonTrovatoException  Un'eccezione che viene lanciata quando la playlist non è stata trovata
     * @return l'oggetto playlist
     * */
    public Playlist doGet(String chiave) throws SQLException {
        if(chiave == null || !chiave.contains(";"))
            throw new IllegalArgumentException("la chiave è null o non valida");

        String[] chiavi = chiave.split(";");
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM playlist PLA WHERE titolo=? && username=?");
        preparedStatement.setString(1,chiavi[0]);
        preparedStatement.setString(2,chiavi[1]);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        if(resultSet.getRow()==0)
            throw new OggettoNonTrovatoException("La playlist non è stata trovata: "+chiave);
        else{
            PlaylistMapper mapper = new PlaylistMapper();
            return mapper.map(resultSet);
        }
    }

    /** Questo metodo salva nel DB la playlist
     *  <p><b>pre: </b>playlist !=null, Titolo della playlist != null, Username dell'utente che ha creato la playlist != null e l'utente non deve avere una playlist con quel titolo<br>
     *     <b>post: </b>la playlist è aggiunta nel db per quel relativo utente</p>
     *  @param playlist la playlist da salvare con i campi titolo e username
     *  @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     *  @throws IllegalArgumentException  Un'eccezione che viene lanciata quando la playlist è null o non valida
     *  @throws OggettoNonInseritoException Un'eccezione che viene lanciata quando la playlist non è stata inserita all'interno del db
     * */

    public void doSave(Playlist playlist) throws SQLException {
        if(playlist == null || playlist.getTitolo() == null || playlist.getUsername() == null)
            throw new IllegalArgumentException("playlist è null o qualche campo obbligatorio è null");
        try {
            doGet(playlist.getTitolo()+";"+playlist.getUsername());
            throw new OggettoGiaPresenteException("La playlist è gia presente");
        }catch (OggettoNonTrovatoException e){
            PreparedStatement ps = connection.prepareStatement(PlaylistQuery.getQueryPlaylistSave());
            ps.setString(1, playlist.getTitolo());
            ps.setString(2,playlist.getUsername());
            ps.setString(3, playlist.getNote());

            if(ps.executeUpdate()!=1)
                throw new OggettoNonInseritoException("La playlist non è stata inserita");
        }
    }


    /** Elimina la playlist dal DB
     * <p><b>pre: </b>chiave != null, chiave.contains(;) == true e la chiave deve essere presente nel db<br>
     *    <b>post: </b>la playlist non deve essere più presente nel db</p>
     * @param chiave concatenazione del titolo della plyalist con la username. Esempio: "playRock;pluto"
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando la chiave è null o non valida
     * @throws OggettoNonCancellatoException Un'eccezione che viene lanciata quando la playlist non è stata cancellata dal db
     * */
    public void doDelete(String chiave) throws SQLException {
        if(chiave == null || !chiave.contains(";"))
            throw new IllegalArgumentException("la chiave è null o non valida");

        try{
            doGet(chiave);
            String[] chiavi = chiave.split(";");
            PreparedStatement st = connection.prepareStatement("DELETE FROM playlist WHERE titolo=? AND username=?");
            st.setString(1,chiavi[0]);
            st.setString(2,chiavi[1]);
            if(st.executeUpdate()!=1)
                throw new OggettoNonCancellatoException("La playlist non è stata cancellata");
        }catch (OggettoNonTrovatoException e){
            throw e;
        }
    }



    /**Questo metodo prende le playlist di un utente
     * <p><b>pre: </b>username != null</p>
     * @param username la username dell utente
     * @param utenteAPI l'interfaccia dell'utente
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando l'username è null o non valida
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando l'utente non è stato trovato nel db
     * @return lista di playlist dell utente
     * */
    public List<Playlist> doRetrievePlaylistByUtente(String username, UtenteAPI utenteAPI) throws SQLException {
        if(username == null )
            throw new IllegalArgumentException("username è null");
        System.out.println(utenteAPI.findUsers("username",username));
        if(utenteAPI.findUsers("username",username).size()==0)
            throw new OggettoNonTrovatoException("l'utente con username: "+username+" non è stato trovato");

        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQueryDoRetrievePlaylistByUtente());
        preparedStatement.setString(1,username);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Playlist> list = new ArrayList<>();
        PlaylistMapper mapper = new PlaylistMapper();
        while (resultSet.next())
            list.add(mapper.map(resultSet));
        return list;
    }

    /**Inserisce una canzone nella playlist di un utente
     * <p><b>pre: </b>username!=null e deve esistere nel db, titoloPlaylist != null e deve esistere nel db, codCanzone != null e deve esistere nel db e deve esistere la playlist di quel dato utente<br>
     *    <b>post: </b>canzone inserita nel db</p>
     * @param username la username dell utente
     * @param titoloPlay il titolo della playlist in cui inserire la canzone
     * @param codCanzone il codice della canzone da inserire
     * @param utenteAPI interfaccia dell'utente
     * @param canzoneAPI interfaccia della canzone
     * @param playlistAPI interfaccia della playlist
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando l'username o titolo o playlist sono null o non valide
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando l'utente non è stato trovato nel db
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando la canzone non è stata trovata nel db
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando la playlist dell'utente non è stata trovata nel db
     * */
    public void doInsertSong(String username, String titoloPlay, String codCanzone, UtenteAPI utenteAPI,
                            CanzoneAPI canzoneAPI,PlaylistAPI playlistAPI) throws SQLException {

        if(username == null || titoloPlay == null || codCanzone == null)
            throw new IllegalArgumentException("username o titolo o codCanzone sono null");

        if(!utenteAPI.exist(username))
            throw new OggettoNonTrovatoException("L'utente non è stato trovato: "+username);

        if(!canzoneAPI.exist(codCanzone))
            throw new OggettoNonTrovatoException("Canzone non è stata trovata: "+codCanzone);

        if(!playlistAPI.isPresent(titoloPlay,username,utenteAPI))
            throw new OggettoNonTrovatoException("Playlist non è stato trovato: "+titoloPlay+" "+ "username");

        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQueryDoInsertSong());
        preparedStatement.setString(1,codCanzone);
        preparedStatement.setString(2,titoloPlay);
        preparedStatement.setString(3,username);
        if(preparedStatement.executeUpdate()!=1)
            throw new OggettoNonInseritoException("La canzone non è stata inserita nella playlist");
    }


    /**Controlla se la canzone è presente nella playlist
     * <p><b>pre: </b>username!=null e deve esistere nel db, titoloPlaylist != null e deve esistere nel db, codCanzone != null e deve esistere nel db e deve esistere la playlist di quel dato utente</p>
     * @param codiceCanzone il codice della canzone da verificare
     * @param titolo il titolo della playlist
     * @param username la username dell utente
     * @param canzoneAPI interfaccia della canzone
     * @param playlistAPI interfaccia della playlist
     * @param utenteAPI interfaccia dell'utente
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando l'username o titolo o playlist sono null o non valide
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando l'utente non è stato trovato nel db
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando la canzone non è stata trovata nel db
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando la playlist dell'utente non è stata trovata nel d
     * @return true se la canzone è presente nella playlist, false altrimenti
     * */
    public boolean isPresent(String codiceCanzone, String titolo, String username, CanzoneAPI canzoneAPI,
                             PlaylistAPI playlistAPI, UtenteAPI utenteAPI) throws SQLException {
        if(codiceCanzone == null || titolo == null || username == null)
            throw new IllegalArgumentException("codiceCanzone o titolo o username sono null");

        if(!utenteAPI.exist(username))
            throw new OggettoNonTrovatoException("L'utente non è stato trovato: "+username);

        if(!canzoneAPI.exist(codiceCanzone))
            throw new OggettoNonTrovatoException("Canzone non è stata trovata: "+codiceCanzone);

        if(!playlistAPI.isPresent(titolo,username,utenteAPI))
            throw new OggettoNonTrovatoException("Playlist non è stato trovato: "+titolo+" "+ "username");

        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQueryIsPresent());
        preparedStatement.setString(1,codiceCanzone);
        preparedStatement.setString(2, titolo);
        preparedStatement.setString(3, username);
        ResultSet resultSet = preparedStatement.executeQuery(); //se le FK sono violate questo lancia SQLException
        return resultSet.next();
    }

    /**Questo metodo controlla se l'utente ha la playlist con quel titolo
     * <p><b>pre: </b>username!=null e deve esistere nel db, titoloPlaylist != null e deve esistere nel db</p>
     * @param titolo il titolo della playlist da verificare
     * @param username username dell'utente
     * @param utenteAPI interfaccia dell'utente
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando l'username o titolo sono null o non valide
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando l'utente non è stato trovato nel db
     * @return true se la playlist è presente, false altrimenti
     * */
    public boolean isPresent(String titolo, String username,UtenteAPI utenteAPI) throws SQLException {
        if(titolo == null || username == null)
            throw  new IllegalArgumentException("titolo o username sono null");

        if(!utenteAPI.exist(username))
            throw new OggettoNonTrovatoException("L'utente non è stato trovato: "+username);

        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQueryIsPresentPlaylist());
        preparedStatement.setString(1, titolo);
        preparedStatement.setString(2, username);
        ResultSet resultSet = preparedStatement.executeQuery(); //se le FK sono violate questo lancia SQLException
        return resultSet.next();
    }

    /**Questo metodo preleva il numero di playlist di un utente
     * <p><b>pre: </b>username!=null e deve esistere nel db</p>
     * @param username username dell'utente
     * @param utenteAPI interfaccia dell'utente
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando l'username è null o non valido
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando l'utente non è stato trovato nel db
     * @return numero di playlist
     * */
    public int doRetrieveNumPlaylistOfUtente(String username, UtenteAPI utenteAPI) throws SQLException {
        if(username == null)
            throw new IllegalArgumentException("username è null");

        if(!utenteAPI.exist(username))
            throw new OggettoNonTrovatoException("L'utente non è stato trovato: "+username);

        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQuerydoRetrieveNumPlaylistOfUtente());
        preparedStatement.setString(1,username);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt("num");
    }


    /** Questo metodo verifica se il titolo è valido secondo la regex
     * <p><b>pre: </b>titolo != null</p>
     * @param titolo titolo della playlsit
     * @return true se è valido secondo la regex, folse altrimenti
     */
    public boolean isValidTitolo(String titolo){
        return TITLE.matcher(titolo).matches();
    }

    /** Questo metodo verifica se la nota è valido secondo la regex
     * <p><b>pre: </b>nota != spazio bianco</p>
     * @param nota della playlsit
     * @return true se è valida secondo la regex, folse altrimenti
     */
    public boolean isValidNota(String nota){
        if(nota == null || nota.length()==0)
            return true;
        /*if(nota.charAt(nota.length()-1)==' ')
            nota+="_";*/

        System.out.println("nota : --"+nota+"--");

        return NOTA.matcher(nota).matches();
    }
    private static final Pattern TITLE = Pattern.compile("^[a-zA-Z0-9_.-. ]{0,100}\\w$");
    private static final Pattern NOTA = Pattern.compile("^[a-zA-Z0-9_.-. ]{0,100}\\w|^$$");

    //metodi non documentati per IS-----------------------------------------------------------------------------------------
    /** Ritorna tutti le playlist*/
    @Generated
    public List<Playlist> doRetrieveAllPlaylist() throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT * FROM playlist;");
        ResultSet rs = st.executeQuery();
        ArrayList<Playlist> lista = new ArrayList<>();
        while(rs.next()){
            lista.add(new PlaylistMapper().map(rs));
        }
        return lista;
    }

    /** Ritorna la lista di playlist che hanno quel titolo*/
    @Generated
    public ArrayList<Playlist> doRetrieveByTitolo(String titolo) throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT * FROM playlist WHERE titolo LIKE ?");
        String titoloLike = "%"+titolo+"%";
        st.setString(1,titoloLike);
        System.out.println(st.toString());
        ResultSet rs = st.executeQuery();
        ArrayList<Playlist> lista = new ArrayList<Playlist>();
        while (rs.next()){

            lista.add(new PlaylistMapper().map(rs));
        }

        return lista;
    }

    /**Rimuove la canzone dalla playlist*/
    @Generated
    public boolean doRemoveSong(String titoloPlaylist, String username, String codice) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQueryDoRemoveSong());
        preparedStatement.setString(1,titoloPlaylist);
        preparedStatement.setString(2,username);
        preparedStatement.setString(3,codice);
        return preparedStatement.executeUpdate()==0;
    }
    /**Ritorna la playlist con le canzoni*/
    @Generated
    public Playlist doRetrievePlaylistWithSongs(String username, String nome) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQueryDoRetrievePlaylistWithSongs());
        preparedStatement.setString(1,username);
        preparedStatement.setString(2,nome);
        ResultSet resultSet = preparedStatement.executeQuery();
        Playlist playlist=null;
        if(resultSet.next()){
            playlist = new PlaylistMapper().map(resultSet);
            playlist.setCanzoni(new ArrayList<>());
            if(resultSet.getString("CAN.codice")!=null)
                playlist.getCanzoni().add(new CanzoneMapper().map(resultSet));
            while (resultSet.next())
                playlist.getCanzoni().add(new CanzoneMapper().map(resultSet));
        }
        return playlist;
    }


    /** Modifica la playlist */
    @Generated
    public void doUpdate(Playlist playlist) throws SQLException {
        PreparedStatement st = connection.prepareStatement(PlaylistQuery.getQueryPlaylistUpdate());
        st.setString(1,playlist.getTitolo());
        st.setString(2,playlist.getNote());
        //st.setDouble(3,playlist.getDurata());
        //st.setDate(4, Date.valueOf(playlist.getDataCreazione()));
        st.setString(5,playlist.getTitolo()); //modificare a 3
        st.setString(6, playlist.getUsername()); //modificare a 4
        if(st.executeUpdate()!=1)
            throw new RuntimeException("delete error");
    }



  /*
    public void doInsertPlaylist(Playlist playlist) throws SQLException {
        if(playlist.getUsername() == null || playlist.getTitolo() == null)
            throw new IllegalArgumentException("titolo o username sono null");

        if(isPresent(playlist.getTitolo(),playlist.getUsername()))
            throw new OggettoGiaPresenteException("La playlist è gia presente");

        PreparedStatement preparedStatement = connection.prepareStatement(PlaylistQuery.getQueryDoInsertPlaylist());
        preparedStatement.setString(1,playlist.getTitolo());
        preparedStatement.setString(2,playlist.getUsername());
        preparedStatement.setString(3,playlist.getNote());
        preparedStatement.setDouble(4,0);
        preparedStatement.setDate(5, Date.valueOf(LocalDate.now()));
        if(preparedStatement.executeUpdate()!=1)
            throw new OggettoNonInseritoException("la playlist non è stata inserita");
    }
*/
}
