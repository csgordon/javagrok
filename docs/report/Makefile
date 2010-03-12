TEXFILE=javagrok
OTHERTEXS=approach.tex conclusion.tex evaluation.tex intro.tex related.tex
DVIFILE=$(TEXFILE:%=%.dvi)
PSFILE=$(TEXFILE:%=%.ps)
PDFFILE=$(TEXFILE:%=%.pdf)
BIBFILE=$(TEXFILE:%=%.bib)

all: pdf

pdf: ps
	#dvipdf -t letter $(DVIFILE) $(PDFFILE)
	ps2pdf $(PSFILE)

ps: dvi
	dvips -t letter -Ppdf -o $(PSFILE) $(DVIFILE)

dvi: $(TEXFILE:%=%.tex) $(BIBFILE)
	latex $(TEXFILE)
	bibtex $(TEXFILE)
	latex $(TEXFILE)
	latex $(TEXFILE)

tidy:
	rm -f *.log

clean: tidy
	rm -f $(DVIFILE) $(PSFILE) $(PDFFILE) *.blg *.aux *.bbl
