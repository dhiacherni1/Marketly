package Services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    boolean ajouter(T t) throws SQLException;
    boolean supprimer(T t) throws SQLException;
    boolean modifier(T t) throws SQLException;
    List<T> afficher() throws SQLException;
    T findById(int id) throws SQLException;
}
