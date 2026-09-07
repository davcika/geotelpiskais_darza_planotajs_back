package lv.darzaplanotajs.address;

import java.util.List;
import java.util.Optional;

/** Interfeiss, lai Postgres meklēšanu vēlāk varētu nomainīt pret ko citu */
public interface AddressSearch {

    /** Pirmā importa laikā tabula vēl ir tukša */
    boolean isReady();

    /** Labākās sakritības pirmās, pieļauj drukas kļūdas un burtus bez garumzīmēm */
    List<AddressResponse> search(String query, int limit);

    Optional<AddressResponse> findByCode(long code);
}
