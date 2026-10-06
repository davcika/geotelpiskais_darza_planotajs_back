-- Bloka rāmja leņķis grādos (dobes lokālais rāmis paralēli dobes malām, sk. BedFrame.java).
-- NULL = vecais bloks, kas glabāts ziemeļu virziena rāmī (leņķis 0); tādus pārrēķina lasot (centrs saglabājas).
ALTER TABLE planting
    ADD COLUMN block_angle DOUBLE PRECISION CHECK (block_angle > -90 AND block_angle < 90);
