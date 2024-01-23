package joserodpt.realmines.api.utils;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pagination<T> extends ArrayList<T> {

    private final int pageSize;

    public Pagination(final int pageSize) {
        this(pageSize, new ArrayList<>());
    }

    @SafeVarargs
    public Pagination(final int pageSize, final T... objects) {
        this(pageSize, Arrays.asList(objects));
    }

    public Pagination(final int pageSize, final List<T> objects) {
        this.pageSize = pageSize;
        this.addAll(objects);
    }

    public int pageSize() {
        return this.pageSize;
    }

    public int totalPages() {
        return (int) Math.ceil((double) this.size() / this.pageSize);
    }

    public boolean exists(final int page) {
        return !(page < 0) && page < this.totalPages();
    }

    public List<T> getPage(final int page) {
        if (page < 0 || page >= this.totalPages())
            throw new IndexOutOfBoundsException("Page: " + page + ", Size: " + this.totalPages());

        final List<T> objects = new ArrayList<>();

        final int min = page * this.pageSize;
        int max = ((page * this.pageSize) + this.pageSize);

        if (max > this.size()) max = this.size();

        for (int i = min; max > i; ++i)
            objects.add(this.get(i));

        return objects;
    }
}