package com.vikalp.portfolio.model;

import java.util.List;

/**
 * One filled order on the career blotter — i.e. a job or a degree.
 *
 * @param period    trading window, e.g. {@code JUL'23—NOW}
 * @param side      BUY for a role, IPO for the listing (education)
 * @param desk      employer / institution
 * @param role      title held on that desk
 * @param fills     individual fills — the things actually shipped
 * @param pnl       headline result for the row
 * @param direction UP / DOWN / FLAT, used only for colouring the P&amp;L cell
 */
public record Execution(
        String period,
        String side,
        String desk,
        String role,
        List<String> fills,
        String pnl,
        String direction
) {
    public boolean open() {
        return period.endsWith("NOW");
    }
}
