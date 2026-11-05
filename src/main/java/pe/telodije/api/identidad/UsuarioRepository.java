package pe.telodije.api.identidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    //Función de busque de usuario por alias o email, extendida del JpaRepostory que brinda la anotación @Repository
    Optional<Usuario> findByAliasOrEmail(String alias, String email);

    //Verificación de existencia por alias, devuelve true o false;
    boolean existsByAlias(String alias);

    //Verificación de existencia por email, devuelve true o false
    boolean existsByEmail(String email);
}