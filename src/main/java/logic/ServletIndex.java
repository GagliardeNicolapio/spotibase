package logic;

import data.Album.AlbumDAO;
import data.Artista.ArtistaDAO;
import data.DAOAcquisto.AcquistoAPI;
import data.DAOAcquisto.AcquistoDAO;
import data.DAOCanzone.CanzoneAPI;
import data.DAOCanzone.CanzoneDAO;
import data.DAOPlaylist.PlaylistAPI;
import data.DAOPlaylist.PlaylistDAO;
import data.DAOUtente.UtenteAPI;
import data.DAOUtente.UtenteDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.util.TreeSet;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@interface Generated {
}

@WebServlet(name = "ServletIndex", value = "/index.html")
public class ServletIndex extends HttpServlet {

    private void getElements(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        CanzoneAPI canzoneAPI = new CanzoneDAO();
        ArtistaDAO artisti= new ArtistaDAO();
        AlbumDAO album = new AlbumDAO();
        //request.setAttribute("canzoni",canzoneAPI.doRetrivePopularSongsWithArtista());
        request.setAttribute("canzoni",canzoneAPI.doRetrieveCanzoneRandom());
        request.setAttribute("artisti",artisti.doRetrieveArtistPopular());
        request.setAttribute("albums",album.doRetrieveAlbumPopular());
        request.setAttribute("artistiCasuali",artisti.doRetrieveArtistRandom());
        request.setAttribute("albumCasuali",album.doRetrieveAlbumRandom());

        request.setAttribute("canzoniNew",canzoneAPI.doRetrieveCanzoniUltimeUscite());
        request.setAttribute("albumNew",album.doRetrieveAlbumUltimeUscite());
        request.setAttribute("canzoniTopBuy",canzoneAPI.doRetrieveCanzoniTopBuy());
        request.setAttribute("albumTopBuy",album.doRetrieveAlbumTopBuy());


        HttpSession session = request.getSession(true);
        if(session.getAttribute("listCart")==null)
            session.setAttribute("listCart", new TreeSet<String>());

        if(session.getAttribute("isLogged")!=null){
            AcquistoAPI acquistoAPI = new AcquistoDAO();
            PlaylistAPI playlistAPI = new PlaylistDAO();
            String username = (String)session.getAttribute("username");
            System.out.println("fatto");
            request.setAttribute("listaPreferiti",canzoneAPI.doRetrieveaCodiciCanzoniPreferite(username));

            request.setAttribute("listaCanzoniAcquistate", acquistoAPI.doRetrieveCodiciCanzoniAcquistate(username));

            UtenteAPI utenteAPI = new UtenteDAO();
            request.setAttribute("listPlaylist", playlistAPI.doRetrievePlaylistByUtente(username,utenteAPI));
        }

    }

    @Override
    @Generated
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try {
            getElements(request,response);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        request.getRequestDispatcher("WEB-INF/views/utente/index.jsp").forward(request,response);

    }

    @Override
    @Generated
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
