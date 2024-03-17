package joserodpt.realmines.api.utils.converters;

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.utils.converters.mrl.CataMinesConverter;
import joserodpt.realmines.api.utils.converters.mrl.MRLConverter;

public enum RMSupportedConverters {

    CATA_MINES("CataMines"),
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
        }
        return null;
    }
}
