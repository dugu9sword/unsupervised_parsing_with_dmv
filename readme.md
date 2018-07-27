# Unsupervised Dependency Parsing with DMV


The code is an implementation of:
- A **left/right-branch** baseline.
- Dan Klein's DMV model ( named "**Dependency Model with Valence**", `ACL-2004` ). 
<!--> - Yong Jiang's neural version ( `EMNLP-2016`  ). (*not implemented yet*)

The directed dependency accuracy(DDA) reported in Klein's paper is about 43.2%. In this implementation, the DDA is sensitive to parameter initialization.
The choosing probability can be initialized in several ways:
- Uniform distribution, the DDA is about ~20%
- Random normal distribution, the DDA is about ~30%
- Allocating a little more choosing probability on `ROOT->VERB`, the DDA is about ~55%
Similar results can be observed in Jiang's neural version ( `EMNLP-2016` ).

Some notes can be found in `note.md`.

## About the Code
Just run the file which is runnable...
I am a little lazy so that I didn't refactor the code carefully...
Maybe there are some useless code segments for debug...

## DataSet

The dataset is `WSJ-PTB` in CoNLL format, below is an example:
```
1	FEDERAL	NNP	4	NAME
2	NATIONAL	NNP	4	NAME
3	MORTGAGE	NNP	4	NAME
4	ASSOCIATION	NNP	0	ROOT
5	Fannie	NNP	6	NAME
6	Mae	NNP	4	PRN
7	Posted	VBN	8	NMOD
```

## Requirements
*See `pom.xml` for details*
- Java 1.8 (or higher)
- Kotlin 1.2 (or higher)
<!--> - DeepLearning4J 0.9.1 (or higher)
<!--> - ND4J 0.9.1 (or higher)

## References

- *Klein, D. and Manning, C.D., 2004, July. Corpus-based induction of syntactic structure: Models of dependency and constituency. In Proceedings of the 42nd Annual Meeting on Association for Computational Linguistics (p. 478). Association for Computational Linguistics.*
- *Klein, D. and Manning, C.D., 2005. The unsupervised learning of natural language structure. Stanford, CA: Stanford University.
 Vancouver*
- *Jiang, Y., Han, W. and Tu, K., 2016. Unsupervised Neural Dependency Parsing. In EMNLP (pp. 763-771).*