/*
 * quiz.c - Trainer per le domande a crocette dell'esame TWEB (teoria)
 *
 * Compila:  gcc -Wall -Wextra -o quiz quiz.c
 * Esegui:   ./quiz            (usa domande.txt nella stessa cartella)
 *           ./quiz altro.txt  (usa un file diverso)
 *
 * TRE tipi di domanda, separati da "---":
 *
 * 1) RISPOSTA SINGOLA
 *   D: testo
 *   A) opzione
 *   CORRETTA: A
 *
 * 2) RISPOSTA MULTIPLA
 *   D: testo
 *   A) opzione
 *   RISPOSTA_MULTIPLA: A, C
 *
 * 3) MATCHING (associa ogni ELEMENTO a una CATEGORIA)
 *   D: testo
 *   ELEMENTI:
 *   A) primo elemento
 *   CATEGORIE:
 *   A) prima categoria
 *   SOLUZIONE: B A   (elemento A -> categoria B, elemento B -> categoria A)
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <ctype.h>

#define C_RESET   "\x1b[0m"
#define C_RED     "\x1b[31m"
#define C_GREEN   "\x1b[32m"
#define C_YELLOW  "\x1b[33m"
#define C_CYAN    "\x1b[36m"
#define C_BOLD    "\x1b[1m"

#define MAX_DOMANDE   200
#define MAX_OPZIONI   12
#define LEN_TESTO     1024
#define LEN_OPZIONE   768
#define LEN_RIGA      2048

enum { T_SINGOLA, T_MULTIPLA, T_MATCHING };

typedef struct {
    int  tipo;
    char testo[LEN_TESTO];
    char opzioni[MAX_OPZIONI][LEN_OPZIONE];
    int  n_opzioni;
    int  corrette[MAX_OPZIONI];
    char elementi[MAX_OPZIONI][LEN_OPZIONE];
    int  n_elementi;
    char categorie[MAX_OPZIONI][LEN_OPZIONE];
    int  n_categorie;
    int  soluzione[MAX_OPZIONI];
} Domanda;

static Domanda domande[MAX_DOMANDE];
static int n_domande = 0;

static void rstrip(char *s) {
    size_t n = strlen(s);
    while (n > 0 && (s[n-1]=='\n' || s[n-1]=='\r' || s[n-1]==' ' || s[n-1]=='\t'))
        s[--n] = '\0';
}
static char *lstrip(char *s) { while (*s==' '||*s=='\t') s++; return s; }

static void segna_corretta(Domanda *d, char lettera) {
    int idx = toupper((unsigned char)lettera) - 'A';
    if (idx >= 0 && idx < d->n_opzioni) d->corrette[idx] = 1;
}

static int carica_domande(const char *path) {
    FILE *f = fopen(path, "r");
    if (!f) { fprintf(stderr, C_RED "Errore: non riesco ad aprire '%s'\n" C_RESET, path); return 0; }
    char riga[LEN_RIGA];
    Domanda *cur = NULL;
    int sezione = 0; /* 0=opzioni/nessuno, 1=elementi, 2=categorie */

    while (fgets(riga, sizeof(riga), f)) {
        rstrip(riga);
        char *p = lstrip(riga);
        if (strcmp(p, "---") == 0) { cur = NULL; sezione = 0; continue; }
        if (*p == '\0') continue;

        if (strncmp(p, "D:", 2) == 0) {
            if (n_domande >= MAX_DOMANDE) break;
            cur = &domande[n_domande++];
            memset(cur, 0, sizeof(*cur));
            cur->tipo = T_SINGOLA; sezione = 0;
            strncpy(cur->testo, lstrip(p + 2), LEN_TESTO - 1);
        }
        else if (strcmp(p, "ELEMENTI:") == 0) { if (cur){ cur->tipo=T_MATCHING; sezione=1; } }
        else if (strcmp(p, "CATEGORIE:") == 0) { if (cur){ cur->tipo=T_MATCHING; sezione=2; } }
        else if (strncmp(p, "SOLUZIONE:", 10) == 0) {
            if (cur) {
                cur->tipo = T_MATCHING;
                int i = 0;
                for (char *c = p + 10; *c && i < cur->n_elementi; c++)
                    if (isalpha((unsigned char)*c)) cur->soluzione[i++] = toupper((unsigned char)*c) - 'A';
                sezione = 0;
            }
        }
        else if (strncmp(p, "CORRETTA:", 9) == 0) {
            if (cur) { cur->tipo = T_SINGOLA; char *v = lstrip(p + 9); if (*v) segna_corretta(cur, *v); }
        }
        else if (strncmp(p, "RISPOSTA_MULTIPLA:", 18) == 0) {
            if (cur) { cur->tipo = T_MULTIPLA;
                for (char *c = p + 18; *c; c++) if (isalpha((unsigned char)*c)) segna_corretta(cur, *c); }
        }
        else if (p[0] >= 'A' && p[0] <= 'Z' && (p[1] == ')' || p[1] == '.')) {
            char *t = lstrip(p + 2);
            if (!cur) continue;
            if (sezione == 1 && cur->n_elementi < MAX_OPZIONI) strncpy(cur->elementi[cur->n_elementi++], t, LEN_OPZIONE-1);
            else if (sezione == 2 && cur->n_categorie < MAX_OPZIONI) strncpy(cur->categorie[cur->n_categorie++], t, LEN_OPZIONE-1);
            else if (sezione == 0 && cur->n_opzioni < MAX_OPZIONI) strncpy(cur->opzioni[cur->n_opzioni++], t, LEN_OPZIONE-1);
        }
    }
    fclose(f);
    return 1;
}

static void shuffle(int *v, int n) {
    for (int i = n - 1; i > 0; i--) { int j = rand()%(i+1); int t=v[i]; v[i]=v[j]; v[j]=t; }
}
static void leggi_input(char *buf, int size) {
    if (fgets(buf, size, stdin) == NULL) { buf[0]='\0'; return; }
    rstrip(buf);
}

static int gioca_scelta(Domanda *d) {
    int map[MAX_OPZIONI];
    for (int i = 0; i < d->n_opzioni; i++) map[i] = i;
    shuffle(map, d->n_opzioni);
    for (int i = 0; i < d->n_opzioni; i++)
        printf("  " C_BOLD "%c)" C_RESET " %s\n", 'A'+i, d->opzioni[map[i]]);
    printf("\nLa tua risposta: ");
    char input[64]; leggi_input(input, sizeof(input));
    if (input[0]=='q'||input[0]=='Q') return -1;
    int scelte[MAX_OPZIONI] = {0};
    for (char *c = input; *c; c++) if (isalpha((unsigned char)*c)) {
        int pos = toupper((unsigned char)*c)-'A';
        if (pos>=0 && pos<d->n_opzioni) scelte[pos]=1;
    }
    int giusta = 1;
    for (int i = 0; i < d->n_opzioni; i++) if (scelte[i] != d->corrette[map[i]]) { giusta=0; break; }
    if (giusta) printf(C_GREEN C_BOLD "\n  Giusto!\n\n" C_RESET);
    else {
        printf(C_RED C_BOLD "\n  Sbagliato.\n" C_RESET);
        printf("  Risposta corretta: " C_GREEN);
        int primo = 1;
        for (int i = 0; i < d->n_opzioni; i++)
            if (d->corrette[map[i]]) { if(!primo) printf(", "); printf("%c",'A'+i); primo=0; }
        printf(C_RESET "\n");
        for (int i = 0; i < d->n_opzioni; i++)
            if (d->corrette[map[i]]) printf("    " C_GREEN "-> %s\n" C_RESET, d->opzioni[map[i]]);
        printf("\n");
    }
    return giusta;
}

static int gioca_matching(Domanda *d) {
    int cmap[MAX_OPZIONI];
    for (int i = 0; i < d->n_categorie; i++) cmap[i] = i;
    shuffle(cmap, d->n_categorie);
    int cinv[MAX_OPZIONI];
    for (int i = 0; i < d->n_categorie; i++) cinv[cmap[i]] = i;

    printf(C_BOLD "  Elementi da classificare:\n" C_RESET);
    for (int i = 0; i < d->n_elementi; i++)
        printf("    " C_CYAN "%c)" C_RESET " %s\n", 'A'+i, d->elementi[i]);
    printf(C_BOLD "\n  Categorie disponibili:\n" C_RESET);
    for (int i = 0; i < d->n_categorie; i++)
        printf("    " C_YELLOW "%c)" C_RESET " %s\n", 'A'+i, d->categorie[cmap[i]]);
    printf("\n  Digita una lettera-categoria per ogni elemento, in ordine (es: BADCE): ");
    char input[64]; leggi_input(input, sizeof(input));
    if (input[0]=='q'||input[0]=='Q') return -1;

    int scelte[MAX_OPZIONI], ns = 0;
    for (char *c = input; *c && ns < d->n_elementi; c++)
        if (isalpha((unsigned char)*c)) scelte[ns++] = toupper((unsigned char)*c)-'A';

    int giusta = (ns == d->n_elementi);
    for (int i = 0; i < d->n_elementi && giusta; i++)
        if (scelte[i] != cinv[d->soluzione[i]]) giusta = 0;

    if (giusta) printf(C_GREEN C_BOLD "\n  Giusto!\n\n" C_RESET);
    else {
        printf(C_RED C_BOLD "\n  Sbagliato.\n" C_RESET);
        printf("  Abbinamento corretto:\n");
        for (int i = 0; i < d->n_elementi; i++)
            printf("    " C_CYAN "%s" C_RESET " -> " C_GREEN "%s\n" C_RESET, d->elementi[i], d->categorie[d->soluzione[i]]);
        printf("\n");
    }
    return giusta;
}

int main(int argc, char *argv[]) {
    const char *path = (argc > 1) ? argv[1] : "domande.txt";
    if (!carica_domande(path)) return 1;
    if (n_domande == 0) { fprintf(stderr, C_RED "Nessuna domanda caricata.\n" C_RESET); return 1; }
    srand((unsigned)time(NULL));

    printf(C_BOLD C_CYAN "\n=== TWEB - Trainer domande a crocette ===\n" C_RESET);
    printf("Caricate %d domande da '%s'.\n", n_domande, path);
    printf("Risposta " C_BOLD "multipla" C_RESET ": lettere insieme, es " C_YELLOW "ACD" C_RESET ".\n");
    printf("Domande " C_BOLD "matching" C_RESET ": una lettera-categoria per elemento, es " C_YELLOW "BADCE" C_RESET ".\n");
    printf("Scrivi " C_YELLOW "q" C_RESET " per uscire.\n\n");

    int ordine[MAX_DOMANDE];
    for (int i = 0; i < n_domande; i++) ordine[i] = i;
    shuffle(ordine, n_domande);

    int punteggio = 0, risposte = 0;
    int sbagliate[MAX_DOMANDE], n_sbagliate = 0;

    for (int k = 0; k < n_domande; k++) {
        Domanda *d = &domande[ordine[k]];
        printf(C_BOLD "Domanda %d/%d" C_RESET, k+1, n_domande);
        if (d->tipo == T_MULTIPLA) printf(C_YELLOW "  (risposta multipla)" C_RESET);
        else if (d->tipo == T_MATCHING) printf(C_YELLOW "  (abbinamento)" C_RESET);
        printf("\n%s\n\n", d->testo);

        int esito = (d->tipo == T_MATCHING) ? gioca_matching(d) : gioca_scelta(d);
        if (esito == -1) { printf(C_YELLOW "\nUscita anticipata.\n" C_RESET); break; }
        risposte++;
        if (esito == 1) punteggio++; else sbagliate[n_sbagliate++] = ordine[k];
        printf("--------------------------------------------------\n\n");
    }

    printf(C_BOLD C_CYAN "=== Riepilogo ===\n" C_RESET);
    printf("Risposte date: %d\n", risposte);
    if (risposte > 0) {
        double perc = 100.0*punteggio/risposte;
        const char *col = (perc>=75.0)?C_GREEN:(perc>=50.0?C_YELLOW:C_RED);
        printf("Corrette: %s%d/%d (%.0f%%)%s\n", col, punteggio, risposte, perc, C_RESET);
    }
    if (n_sbagliate > 0) {
        printf(C_BOLD C_YELLOW "\n=== Ripasso delle domande sbagliate ===\n\n" C_RESET);
        for (int s = 0; s < n_sbagliate; s++) {
            Domanda *d = &domande[sbagliate[s]];
            printf(C_BOLD "%d. %s\n" C_RESET, s+1, d->testo);
            if (d->tipo == T_MATCHING)
                for (int i = 0; i < d->n_elementi; i++)
                    printf("   " C_GREEN "%s -> %s\n" C_RESET, d->elementi[i], d->categorie[d->soluzione[i]]);
            else
                for (int i = 0; i < d->n_opzioni; i++)
                    if (d->corrette[i]) printf("   " C_GREEN "[corretta] %s\n" C_RESET, d->opzioni[i]);
            printf("\n");
        }
    } else if (risposte > 0) {
        printf(C_GREEN C_BOLD "\nTutte giuste! In bocca al lupo per l'esame.\n" C_RESET);
    }
    return 0;
}
