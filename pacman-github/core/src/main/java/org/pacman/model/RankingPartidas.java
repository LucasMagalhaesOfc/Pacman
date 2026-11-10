package org.pacman.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Ranking local de VITORIAS: mais pontos por minuto = melhor colocacao.
 * Nao depende de LibGDX, para permitir testes de persistencia sem interface. */
public final class RankingPartidas {
    public static final int LIMITE_SALVO = 100;
    public static final int LIMITE_VISIVEL = 8;

    public static final class Resultado {
        public final String nome;
        public final int pontos;
        public final long tempoMs;

        public Resultado(String nome, int pontos, long tempoMs) {
            this.nome = normalizarNome(nome);
            this.pontos = Math.max(0, pontos);
            this.tempoMs = Math.max(1000L, tempoMs);
        }
        public double pontosPorMinuto() {
            return pontos * 60000.0 / tempoMs;
        }
        public String tempoFormatado() {
            long segundos = tempoMs / 1000;
            return String.format(Locale.US, "%02d:%02d", segundos/60, segundos%60);
        }
    }

    private static final Comparator<Resultado> CRITERIO = new Comparator<Resultado>() {
        @Override public int compare(Resultado a, Resultado b) {
            // Multiplicacao em double para evitar overflow com tempos grandes.
            int taxa = Double.compare(b.pontosPorMinuto(), a.pontosPorMinuto());
            if (taxa != 0) return taxa;
            int pts = Integer.compare(b.pontos, a.pontos);
            if (pts != 0) return pts;
            return Long.compare(a.tempoMs, b.tempoMs);
        }
    };

    private final ArrayList<Resultado> historico = new ArrayList<Resultado>();

    /** Letras ASCII, numeros, espaco, hifen e sublinhado; sem delimitadores da gravacao. */
    public static String normalizarNome(String texto) {
        if (texto == null) return "";
        StringBuilder limpo = new StringBuilder();
        String upper = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT);
        for (int i=0;i<upper.length() && limpo.length()<16;i++) {
            char c=upper.charAt(i);
            if ((c>='A'&&c<='Z')||(c>='0'&&c<='9')||c==' '||c=='-'||c=='_')
                limpo.append(c);
        }
        return limpo.toString().trim().replaceAll(" +", " ");
    }

    public void registrarVitoria(String nome, int pontos, float segundos) {
        String normalizado=normalizarNome(nome);
        if (normalizado.length()==0 || pontos<=0 || segundos<=0f) return;
        historico.add(new Resultado(normalizado,pontos,Math.round(segundos*1000d)));
        ordenarELimitar();
    }

    private void ordenarELimitar() {
        Collections.sort(historico, CRITERIO);
        while (historico.size()>LIMITE_SALVO) historico.remove(historico.size()-1);
    }
    public List<Resultado> melhores(int quantidade) {
        int limite=Math.max(0,Math.min(quantidade,historico.size()));
        return Collections.unmodifiableList(new ArrayList<Resultado>(historico.subList(0,limite)));
    }
    public int quantidade() { return historico.size(); }

    public String serializar() {
        StringBuilder s=new StringBuilder();
        for(Resultado r:historico)
            s.append(r.nome).append('|').append(r.pontos).append('|')
                .append(r.tempoMs).append('\n');
        return s.toString();
    }

    public static RankingPartidas carregar(String texto) {
        RankingPartidas ranking=new RankingPartidas();
        if (texto==null || texto.isEmpty()) return ranking;
        for(String linha:texto.split("\\R")) {
            String[] partes=linha.split("\\|",-1);
            if(partes.length!=3)continue;
            try {
                int pontos=Integer.parseInt(partes[1]);
                long tempo=Long.parseLong(partes[2]);
                if(pontos<=0 || tempo<1000 || normalizarNome(partes[0]).isEmpty())continue;
                ranking.historico.add(new Resultado(partes[0],pontos,tempo));
            } catch(NumberFormatException ignorado) { /* registro danificado nao trava o jogo */ }
        }
        ranking.ordenarELimitar();
        return ranking;
    }
}
