package joserodpt.realmines.api.utils.converters;

import joserodpt.realmines.api.RealMinesAPI;

public enum RMSupportedConverters {

    CATA_MINES("CataMines"),
    JETS_PRISON_MINES("JetsPrisonMines"),
    MINE_RESET_LITE("MineResetLite");

    private final String source;

    RMSupportedConverters(String source) {
        this.source = source;
    }

    public String getSourceName() {
        return this.source;
    }

    public RMConverterBase getConverter(RealMinesAPI api) {
        switch (this) {
            case CATA_MINES:
                return new CataMinesConverter(api);
            case MINE_RESET_LITE:
                return new MRLConverter(api);
            case JETS_PRISON_MINES:
                return new JetsPrisonMinesConverter(api);
        }
        return null;
    }
}
