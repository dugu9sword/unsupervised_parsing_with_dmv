#NOTE

- 通过初始化 root 的 stop 约束由于 root 不可能被 attach 两次，此时 root 的 outside 仅 (root, 0, senSize - 1) 非 0。因为其他情况会出现 attach。

- unsealed 与 sealed：
    - count(unsealed) 大，因为存在树结构的包含关系，count 是整棵树的概率，count(unsealed) = count(sealed) + other
    - inside(unsealed) 大，inside 是部分树的 potential，因为 inside(sealed) = stop * inside(unsealed)
    - outside(*) 无法确定大小，outside 是部分树的 potential，虽然存在树结构的大小关系，但是 outside(unsealed) = stop * outside(sealed) + other，由于有一个 stop 因子的折扣，无法比较大小

|TAG | INFO | MORE|
|--- | --- | ---|
|CC		|Coordinating conjunction | and/neither 等并列连词|
|CD		|Cardinal number | 量词|
|DT		|Determiner | 限定词|
|EX		|Existential there | there 之类的存在句|
|FW		|Foreign word | 外来词|
|IN		|Preposition or subordinating conjunction | in/on/of/after 等介词|
|JJ		|Adjective | 形容词|
|JJR	|Adjective, comparative | 形容词比较级|
|JJS	|Adjective, superlative | 形容词最高级|
|LS		|List item marker | 有的句子是列表的一部分，以数字开头|
|MD		|Modal | could/would/can 等语气词|
|NN		|Noun, singular or mass | 单数名词|
|NNS	|Noun, plural | 复数名词|
|NNP	|Proper noun, singular | 单数专有名词|
|NNPS   |Proper noun, plural | 复数专有名词|
|PDT	|Predeterminer | half/such/all 等位于限定词前面的词|
|POS	|Possessive ending | '/'s 等所有格|
|PRP	|Personal pronoun | it/he/we 等人称代词|
|PRP$	|Possessive pronoun | his/her 等人称代词所有格，物主代词|
|RB		|Adverb | 副词|
|RBR	|Adverb, comparative | 比较级副词|
|RBS	|Adverb, superlative | 最高级副词|
|RP		|Particle | up/down 等与动词构成动词短语的副词或介词|
|SYM	|Symbol | 符号|
|TO		|to | to|
|UH		|Interjection | oh/no/yes/ah 等感叹词|
|VB		|Verb, base form | 动词|
|VBD	|Verb, past tense | 过去时|
|VBG	|Verb, gerund or present participle | 现在分词|
|VBN	|Verb, past participle | 过去分词|
|VBP	|Verb, non-3rd person singular present | 第三人称复数|
|VBZ	|Verb, 3rd person singular present | 第三人称单数|
|WDT	|Wh-determiner | 定语从句中的 which/that 等定语|
|WP		|Wh-pronoun | who/what 等|
|WP$	|Possessive wh-pronoun | whose 等所有格|
|WRB	|Wh-adverb | when/how 等|
