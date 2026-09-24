package lv.darzaplanotajs.plant;

/** score ir pg_trgm līdzība no 0 līdz 1 */
public record SimilarPlant(long id, String nameEn, String nameLv, double score) {
}
