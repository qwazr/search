/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import java.util.ArrayList;
import java.util.List;

public class Paging {

    private final Page prev;
    private final Page next;
    private final Page current;
    private final List<Page> pages;
    private final Integer totalPages;
    private final int pageSize;

    public Paging(Long totalHit, long currentStart, int pageSize, int numberOfPages) {
        this.pageSize = pageSize;
        if (totalHit == null || totalHit <= 0) {
            prev = null;
            next = null;
            current = null;
            pages = null;
            totalPages = null;
            return;
        }

        totalPages = (int) ((totalHit + (pageSize - 1)) / pageSize);

        int currentPage = currentStart == 0 ? 0 : (int) currentStart / pageSize;
        if (currentPage >= totalPages)
            currentPage = totalPages - 1;

        int startPage = currentPage - (numberOfPages / 2);
        if (startPage < 0)
            startPage = 0;

        int endPage = startPage + numberOfPages;
        if (endPage > totalPages)
            endPage = totalPages;

        this.pages = new ArrayList<>(endPage - startPage);

        for (int i = startPage; i < endPage; i++) {
            final Page page = new Page(i);
            pages.add(page);
            if (page.start + pageSize >= totalHit)
                break;
        }
        this.current = new Page(currentPage);
        this.prev = currentPage > 0 ? new Page(currentPage - 1) : null;
        this.next = currentPage + 1 >= totalPages ? null : new Page(currentPage + 1);
    }

    public Page getPrev() {
        return prev;
    }

    public Page getNext() {
        return next;
    }

    public Page getCurrent() {
        return current;
    }

    public Integer getTotalPage() {
        return totalPages;
    }

    public List<Page> getPages() {
        return pages;
    }

    Page of(int page) {
        return new Page(page);
    }

    public class Page {

        private final long start;
        private final int number;

        Page(int page) {
            this.start = (long) page * pageSize;
            this.number = page + 1;
        }

        public long getStart() {
            return start;
        }

        public int getNumber() {
            return number;
        }

        public boolean isCurrent() {
            return equals(current);
        }

        @Override
        public int hashCode() {
            return Long.hashCode(start);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Page))
                return false;
            if (o == this)
                return true;
            final Page p = (Page) o;
            return p.start == start && p.number == number;
        }

        @Override
        public String toString() {
            return number + " - " + start;
        }

    }
}
