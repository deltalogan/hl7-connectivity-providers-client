package com.hl7client.model.dental;

import static com.hl7client.model.dental.DentalPieceType.*;

public enum DentalPiece {

    P11(INCISIVO), P12(INCISIVO),
    P13(CANINO),
    P14(PREMOLAR), P15(PREMOLAR),
    P16(MOLAR), P17(MOLAR), P18(MOLAR),

    P21(INCISIVO), P22(INCISIVO),
    P23(CANINO),
    P24(PREMOLAR), P25(PREMOLAR),
    P26(MOLAR), P27(MOLAR), P28(MOLAR),

    P31(INCISIVO), P32(INCISIVO),
    P33(CANINO),
    P34(PREMOLAR), P35(PREMOLAR),
    P36(MOLAR), P37(MOLAR), P38(MOLAR),

    P41(INCISIVO), P42(INCISIVO),
    P43(CANINO),
    P44(PREMOLAR), P45(PREMOLAR),
    P46(MOLAR), P47(MOLAR), P48(MOLAR);

    private final DentalPieceType type;

    DentalPiece(DentalPieceType type) {
        this.type = type;
    }

    // =====================
    // API pública
    // =====================

    public DentalPieceType getType() {
        return type;
    }

    /**
     * Código FDI (ej: 11, 26, 48)
     */
    public String getFdiCode() {
        return name().substring(1);
    }

    /**
     * Factory segura desde número FDI
     *
     * @param fdi 11..48
     * @return DentalPiece correspondiente
     */
    public static DentalPiece fromFdi(int fdi) {
        String key = "P" + fdi;

        try {
            return DentalPiece.valueOf(key);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Código FDI inválido o no soportado: " + fdi, ex
            );
        }
    }
}
