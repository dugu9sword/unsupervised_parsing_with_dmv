# Unsupervised Dependency Parsing with DMV


The code is an implementation of:
- A **left/right-branch** baseline.
- Dan Klein's DMV model ( named "**Dependency Model with Valence**", `ACL-2004` ). 
- Yong Jiang's neural version ( `EMNLP-2016`  ). (*not implemented yet*)

Some notes can be found in `note.md`.

## Requirements
*See `pom.xml` for details*
- Java 1.8 (or higher)
- Kotlin 1.2 (or higher)
- DeepLearning4J 0.9.1 (or higher)
- ND4J 0.9.1 (or higher)

## References

- *Klein, D. and Manning, C.D., 2004, July. Corpus-based induction of syntactic structure: Models of dependency and constituency. In Proceedings of the 42nd Annual Meeting on Association for Computational Linguistics (p. 478). Association for Computational Linguistics.*
- *Klein, D. and Manning, C.D., 2005. The unsupervised learning of natural language structure. Stanford, CA: Stanford University.
 Vancouver*
- *Jiang, Y., Han, W. and Tu, K., 2016. Unsupervised Neural Dependency Parsing. In EMNLP (pp. 763-771).*