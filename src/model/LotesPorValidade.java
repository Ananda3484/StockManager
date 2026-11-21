package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Padrão: Iterator - percorre os lotes pela menor validade (FEFO) sem expor a lista original. */
public class LotesPorValidade implements Iterable<Lote> {
    private final List<Lote> lotes;

    public LotesPorValidade(List<Lote> lotes) {
        this.lotes = lotes;
    }

    @Override
    public Iterator<Lote> iterator() {
        return new IteradorValidade(lotes);
    }

    private static class IteradorValidade implements Iterator<Lote> {
        private final List<Lote> ordenados;
        private int posicao = 0;

        IteradorValidade(List<Lote> lotes) {
            ordenados = new ArrayList<>(lotes);
            ordenados.sort(Comparator.comparing(Lote::getValidade).thenComparing(Lote::getNumero));
        }

        @Override
        public boolean hasNext() {
            return posicao < ordenados.size();
        }

        @Override
        public Lote next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return ordenados.get(posicao++);
        }
    }
}
