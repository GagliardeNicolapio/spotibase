package data.DAOCanzone;


import data.Exceptions.OggettoGiaPresenteException;
import data.Exceptions.OggettoNonCancellatoException;
import data.Exceptions.OggettoNonInseritoException;
import data.Exceptions.OggettoNonTrovatoException;
import data.utils.SingletonJDBC;
import data.Artista.Artista;
import data.Artista.ArtistaMapper;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@interface Generated {
}
/**Questa classe permette  di gestire le operazioni relative ai dati persistenti della canzone.
 * @version 1.0
 * @see CanzoneAPI interfaccia della classe
 * */
public class CanzoneDAO implements CanzoneAPI {
    
    Connection connection;
    public CanzoneDAO(){
        this.connection = SingletonJDBC.getConnection();
    }
    public CanzoneDAO(Connection connection) {
        this.connection = connection;
    }
    //metodi documentati per IS----------------------------------------------------------------------------------------------


    /**Questo metodo consente di verificare se la canzone è all’interno del database
     * <p><b>pre: </b>codice != null </p>
     * @param codice codice della canzone
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando il codice della canzone è null o non valido
     * @return true se la canzone esiste, false altrimenti
     */
    public boolean exist(String codice) throws SQLException {
        if(codice == null)
            throw new IllegalArgumentException("codice è null");
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM canzone CAN WHERE CAN.codice = ?");
        preparedStatement.setString(1, codice);
        return preparedStatement.executeQuery().next();
    }


    /**Questo metodo preleva una canzone salvata nel db
     * <p><b>pre:</b> la chiave != null e presente nel db</p>
     * @param chiave la primary key della canzone
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando la chiave è null o non valida
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando la canzone non è stata trovata nel db
     * @return oggetto canzone
     * */
    public Canzone doGet(String chiave) throws SQLException {
        if(chiave == null)
            throw new IllegalArgumentException("chiave è null");

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM canzone CAN WHERE CAN.codice = ?");
        preparedStatement.setString(1,chiave);
        ResultSet resultSet = preparedStatement.executeQuery();

        resultSet.next();
        if(resultSet.getRow() == 0)
            throw new OggettoNonTrovatoException("canzone non trovata nel db");

        CanzoneMapper mapper = new CanzoneMapper();
        return mapper.map(resultSet);
    }

    /** Salva nel DB la canzone
     * <p><b>pre: </b>canzone != null e non deve esistere nel db, il codice della canzone!= null e titolo != null<br>
     *    <b>post: </b>canzone presente nel db</p>
     * @param canzone la canzone da salvare. Con titolo e codice settati
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando la canzone o il codice della canzone oppure il titolo della canzone sono null o non validi
     * @throws OggettoGiaPresenteException  Un'eccezione che viene lanciata quando la canzone  è già presente nel db
     * @throws OggettoNonInseritoException Un'eccezione che viene lanciata quando la canzone non è stata inserita
     * */
    public void doSave(Canzone canzone) throws SQLException {
        if(canzone == null || canzone.getTitolo() == null || canzone.getCodice() == null)
            throw new IllegalArgumentException("canzone o titolo o codice sono null");

        try{
            doGet(canzone.getCodice());
            throw new OggettoGiaPresenteException("La canzone è gia presente");
        }catch (OggettoNonTrovatoException e){
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO canzone VALUES (?,?,?,?,?,?,?,?)");
            preparedStatement.setString(1,canzone.getCodice());
            preparedStatement.setInt(2,canzone.getAnno());
            preparedStatement.setDouble(3,canzone.getDurata());
            preparedStatement.setString(4,canzone.getTitolo());
            preparedStatement.setDouble(5,canzone.getPrezzo());
            if(canzone.getAlbum()==null)
                preparedStatement.setString(6, null);
            else
                preparedStatement.setString(6,canzone.getAlbum().getCodice());
            preparedStatement.setString(7,canzone.getPathImg());
            preparedStatement.setString(8,canzone.getPathMP3());
            if(preparedStatement.executeUpdate()!=1)
                throw new OggettoNonInseritoException("La canzone non è stata inserita");
        }
    }


    /** Elimina la canzone dal DB
     * <p><b>pre: </b>codice della canzone != null e già presente nel db<br>
     *    <b>post: </b>canzone eliminata dal db</p>
     * @param codCanzone codice della canzone da eliminare
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando il codice della canzone è null o non valida
     * @throws OggettoNonCancellatoException  Un'eccezione che viene lanciata quando la canzone non è stata cancellata nel db
     * */
    public void doDelete(String codCanzone) throws SQLException {
        if(codCanzone == null)
            throw new IllegalArgumentException("codCanzone è null");

        try{
            doGet(codCanzone);
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM canzone WHERE codice=?");
            preparedStatement.setString(1,codCanzone);
            if(preparedStatement.executeUpdate()!=1)
                throw new OggettoNonCancellatoException("La canzone non è stata cancellata");
        }catch (OggettoNonTrovatoException e){
            throw e;
        }
    }



    /**Ritorna la canzone con la lista degli artisti
     * <p><b>pre: </b>codice della canzone!= null </p>
     * @param codiceCanzone il codice della canzone da prelevare
     * @throws SQLException Un'eccezione che fornisce informazioni su un errore di accesso al database o altri errori.
     * @throws IllegalArgumentException  Un'eccezione che viene lanciata quando il codice della canzone è null o non valido
     * @throws OggettoNonTrovatoException Un'eccezione che viene lanciata quando la canzone non è stata trovata nel db
     * @return la canzone con all'interno anche la lista degli artisti
     * */
    public Canzone doRetrieveCanzoneWithArtisti(String codiceCanzone) throws SQLException {
        if(codiceCanzone == null)
            throw new IllegalArgumentException("codiceCanzone è null");

        try{
            System.out.println(codiceCanzone+"-----------------------");
            doGet(codiceCanzone);
            PreparedStatement statement = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveCanzoneWithArtisti());
            statement.setString(1,codiceCanzone);
            ResultSet resultSet = statement.executeQuery();
            Canzone canzone=null;
            if(resultSet.next()){
                canzone = new CanzoneMapper().map(resultSet);
                canzone.setArtisti(new ArrayList<>());
                canzone.getArtisti().add(new ArtistaMapper().map(resultSet));
                while (resultSet.next())
                    canzone.getArtisti().add(new ArtistaMapper().map(resultSet));
            }
            return canzone;
        }catch (OggettoNonTrovatoException e){
            throw e;
        }
    }




    //metodi NON documentati per IS----------------------------------------------------------------------------------------------

    /**Ritorna le ultime uscite*/
    @Generated
    public List<Canzone> doRetrieveCanzoniUltimeUscite() throws SQLException {
            PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetriveCanzoniUltimeUscite());
            ResultSet rs = st.executeQuery();
            ArrayList<Canzone> lista = new ArrayList<>();
            while(rs.next())
                lista.add(new CanzoneMapper().map(rs));

            return lista;
    }

    /**Ritorna le canzoni con piu preferenze con la lista degli artisti*/
    @Generated
    public List<Canzone> doRetrivePopularSongsWithArtista() throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetrievePopularSongsWithArtista());
        ResultSet resultSet= st.executeQuery();
        LinkedHashMap<String,Canzone> mappaCanzone= new LinkedHashMap<>();
        CanzoneMapper canzoneMapper= new CanzoneMapper();
        Canzone canzone;
        ArtistaMapper artistaMapper= new ArtistaMapper();
        while(resultSet.next()){
            canzone= canzoneMapper.map(resultSet);
            if(!mappaCanzone.containsKey(canzone.getCodice())){
                mappaCanzone.put(canzone.getCodice(),canzone);
                canzone.setArtisti(new ArrayList<Artista>());
                canzone.getArtisti().add(artistaMapper.map(resultSet));
            }else
                mappaCanzone.get(canzone.getCodice()).getArtisti().add(artistaMapper.map(resultSet));


        }
        return new ArrayList<>(mappaCanzone.values());

    }

    /**Ritorna una lista di canzoni random*/
    @Generated
    public List<Canzone> doRetrieveCanzoneRandom() throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetriveCanzoneRandom());
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while(rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
    }



    /**Ritorna i codici delle canzoni preferite dall utente*/
    @Generated
    public List<String> doRetrieveaCodiciCanzoniPreferite(String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveCodiciCanzoniPreferite());
        preparedStatement.setString(1,username);
        ArrayList<String> codici = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
            codici.add(resultSet.getString("PRE.codiceCanzone"));
        return codici;
    }

    @Generated
   public List<Canzone> doRetrieveSongsByPlaylist(String username, String titoloPlaylist) throws SQLException {

            PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveSongsByPlaylist());
            String usernameLike = "%"+username+"%";
            String titoloLike = "%"+titoloPlaylist+"%";
            st.setString(1,usernameLike);
            st.setString(2,titoloLike);
            ResultSet rs = st.executeQuery();
            ArrayList<Canzone> lista = new ArrayList<>();
            while(rs.next())
                lista.add(new CanzoneMapper().map(rs));

            return lista;
    }
    @Generated
   public List<Canzone> doRetrieveSongsByAlbum(String codAlbum) throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveSongsByAlbum());
        String codAlbumLike = "%"+codAlbum+"%";
        st.setString(1,codAlbumLike);
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while(rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
    }
    @Generated
   public List<Canzone> doRetrieveSinglesByArtista(String nomeDArte) throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveSinglesByArtista());
        String nomeDArteLike = "%"+nomeDArte+"%";
        st.setString(1,nomeDArteLike);
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while (rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
    }

    /**Ritorna le canzoni dell artista*/
    @Generated
   public List<Canzone> doRetrieveSongsByArtista(String nomeDArte) throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveSongsByArtista());
        String nomeDArteLike = "%"+nomeDArte+"%";
        st.setString(1,nomeDArteLike);
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while (rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
    }

    /**Ritorna le canzoni con piu preferenze*/
    @Generated
   public List<Canzone> doRetrieveCanzonePopular() throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveCanzonePopular());
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while(rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
    }

    /** Ritorna la lista di canzoni che hanno quel titolo */
    @Generated
   public List<Canzone> doRetrieveByName(String titolo) throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT * FROM canzone CAN WHERE CAN.titolo LIKE ?");
        String titleLike = "%"+titolo+"%";
        st.setString(1,titleLike);
        System.out.println(st.toString());
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while (rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
   }



   /** Modifica la canzone */
   @Generated
   public boolean doUpdate(Canzone canzone) throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryCanzoneUpdate());
        st.setString(1,canzone.getTitolo());
        st.setInt(2,canzone.getAnno());
        st.setDouble(3,canzone.getDurata());
        st.setDouble(4,canzone.getPrezzo());
        st.setString(5,canzone.getCodice());
        return st.executeUpdate()==1;
   }

   /** Ritorna tutti le canzoni*/
   @Generated
   public List<Canzone> doRetrieveAllCanzoni() throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT * FROM canzone CAN;");
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while(rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
   }
    @Generated
   public List<Canzone> doRetrieveAllCanzoni(int offset, int numElements) throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT * FROM canzone CAN ORDER BY CAN.titolo LIMIT ?,?;");
        st.setInt(1,offset);
        st.setInt(2,numElements);
        ResultSet rs = st.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while(rs.next())
            lista.add(new CanzoneMapper().map(rs));

        return lista;
   }

   /** Ritorna le canzoni del genere passato*/
   @Generated
   public List<Canzone> doRetrieveCanzoneByGenere(String genere) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(CanzoneQuery.getQueryCanzoneByGenere());
        ps.setString(1,'%'+genere+'%');
        List<Canzone> canzoni = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            canzoni.add(new CanzoneMapper().map(rs));

        return canzoni;
    }
    @Generated
    public List<Canzone> doRetrieveCanzoneByGenere(String genere, String filterSQL, int offset, int numElements) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(CanzoneQuery.getQueryCanzoneByGenere(filterSQL));
        preparedStatement.setString(1,genere);
        preparedStatement.setInt(2,offset);
        preparedStatement.setInt(3,numElements);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Canzone> list = new ArrayList<>();
        CanzoneMapper canzoneMapper = new CanzoneMapper();
        while(resultSet.next())
            list.add(canzoneMapper.map(resultSet));
        return list;
    }

    /**Ritorna il numero di canzoni nel DB*/
    @Generated
   public int doRetrieveNumCanzoni() throws SQLException {
       PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) NUM FROM canzone;");
       ResultSet resultSet = statement.executeQuery();
       if(resultSet.next())
           return resultSet.getInt("NUM");
       else return 0;
   }
   /*devo sapere quante canzoni ho nel database. Se ne tengo 100 e ad ogni pagina ne mosto 10 allora 100/10=10pagine
   * se pero ne tengo 92 => 92\10=9.2 arrotondo sempre per eccesso e quindi devo fare 10 pagine
   * */

    /**Ritorna la somma dei guadagni dalle canzoni acquistate*/
    @Generated
    public double doRetrieveTotaleGuadagno() throws SQLException {
        PreparedStatement st = connection.prepareStatement(CanzoneQuery.getQueryTotaleGuadagni());
        ResultSet rs = st.executeQuery();
        if(!rs.next())
            return 0;
        else return rs.getDouble("totale");
    }

    /**Ritorna le canzoni piu acquistate*/
    @Generated
    public List<Canzone> doRetrieveCanzoniTopBuy() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveCanzoniTopBuy());
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Canzone> lista = new ArrayList<>();
        while (resultSet.next())
            lista.add(new CanzoneMapper().map(resultSet));
        return lista;
    }

    /**Ritorna la canzone con quel codice*/
    @Generated
    public Canzone doRetrieveCanzoneByCodice(String codice) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveAlbumByCodice());
        preparedStatement.setString(1,codice);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next())
            return new CanzoneMapper().map(resultSet);
        else return null;
    }

    /**Ritorna la lista di canzoni in base ai codici passati, con la lista artisti*/
    @Generated
    public List<Canzone> doRetrieveCanzoniByCodiciWithArtisti(List<String> codici) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(CanzoneQuery.getQueryDoRetrieveCanzoniByCodici(codici.size()));
        for(int i=0; i<codici.size(); i++)
            preparedStatement.setString(1+i,codici.get(i));

        ResultSet resultSet = preparedStatement.executeQuery();
        LinkedHashMap<String, Canzone> mappa = new LinkedHashMap<>();
        while (resultSet.next()){
            Canzone canzone = new CanzoneMapper().map(resultSet);
            if(!mappa.containsKey(canzone.getCodice())){
                canzone.setArtisti(new ArrayList<>());
                mappa.put(canzone.getCodice(), canzone);
                canzone.getArtisti().add(new ArtistaMapper().map(resultSet));
            }else mappa.get(canzone.getCodice()).getArtisti().add(new ArtistaMapper().map(resultSet));
        }
        return new ArrayList<>(mappa.values());
    }


/*

    public boolean doInsertPreferenza(String codice, String username){
        try(Connection connection = ConPool.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(CanzoneQuery.getQueryDoInsertPreferenza());
            preparedStatement.setString(1,codice);
            preparedStatement.setString(2,username);
            return preparedStatement.executeUpdate()!=0;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }


    public boolean doRemovePreferenza(String codice, String username){
        try(Connection connection = ConPool.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(CanzoneQuery.getQueryDoDeletePreferenza());
            preparedStatement.setString(1,codice);
            preparedStatement.setString(2,username);
            return preparedStatement.executeUpdate()!=0;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
*/
}