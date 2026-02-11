package dev.svrt.dominik.warpbook.data;

public enum WarpPageBindingType {

    POSITION("Warp_Page_Bound"),
    ENTITY("Warp_Page_Entity_Bound");

    public final String itemType;

    WarpPageBindingType(String itemType) {
        this.itemType = itemType;
    }
}
