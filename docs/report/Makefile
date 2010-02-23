TEXFILE=javagrok
OTHERTEXS=approach.tex conclusion.tex evaluation.tex intro.tex related.tex
DVIFILE=$(TEXFILE:%=%.dvi)
PSFILE=$(TEXFILE:%=%.ps)
PDFFILE=$(TEXFILE:%=%.pdf)
BIBFILE=$(TEXFILE:%=%.bib)

all: pdf

pdf: dvi
	dvipdf $(DVIFILE) $(PDFFILE)
	
ps: dvi
	dvips $(DVIFILE) -o $(PSFILE)

dvi: $(TEXFILE:%=%.tex) $(BIBFILE)
	latex $(TEXFILE)
	bibtex $(TEXFILE)
	latex $(TEXFILE)
	latex $(TEXFILE)

tidy:
	rm -f *.log

clean: tidy
	rm -f $(DVIFILE) $(PSFILE) $(PDFFILE) *.blg *.aux *.bbl
