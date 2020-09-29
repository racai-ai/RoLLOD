import argparse

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_path")
    parser.add_argument("output_path")
    parser.add_argument("source_url")

    args = parser.parse_args()

    sent_counter = 1
    word_counter = 1
    parsed_sent = False
    with open(args.input_path, "r", encoding="utf-8") as inf, open(args.output_path, "w", encoding="utf-8") as outf:
        outf.write('<?xml version="1.0" encoding="UTF-8"?>\n'
                   '<rdf:RDF\n'
                   '\txmlns="{}#"\n'
                   '\txmlns:conll="http://ufal.mff.cuni.cz/conll2009-st/task-description.html#"\n'
                   '\txmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"\n'
                   '\txmlns:terms="http://purl.org/acoli/open-ie/"\n'
                   '\txmlns:nif="http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#"\n'
                   '\txmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">\n\n'.format(args.source_url))

        for line in inf:
            if line.startswith("@") or line.startswith("#"):
                pass
            elif line == "\n":
                pass
            elif line.startswith(":s{}_0 a nif:Sentence; rdfs:comment".format(sent_counter)):
                outf.write("<rdf:Description rdf:about=\"{}#s{}_0\">\n".format(args.source_url, sent_counter))
                outf.write("\t<rdf:type rdf:resource=\"http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Sentence\"/>\n")
                outf.write("</rdf:Description>\n\n")

                parsed_sent = True
                word_counter = 1
            elif line.startswith(":s{}_0 nif:nextSentence :".format(sent_counter)):
                outf.write('<rdf:Description rdf:about="{}#s{}_0">\n'.format(args.source_url, sent_counter))
                outf.write('\t<nif:nextSentence rdf:resource="{}#s{}_0"/>\n'.format(args.source_url, sent_counter + 1))
                outf.write('</rdf:Description>\n\n'.format(args.source_url, sent_counter))

                sent_counter += 1
            elif line.startswith(':s{}_{} a nif:Word;'.format(sent_counter, word_counter)):
                tokens = line.split("; ")

                outf.write('<rdf:Description rdf:about="{}#s{}_{}">\n'.format(args.source_url, sent_counter, word_counter))
                outf.write('\t<rdf:type rdf:resource="http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Word"/>\n')

                for token in tokens[1:]:
                    if "HEAD" in token:
                        data = token.split()[1][1:]
                        outf.write('\t<conll:HEAD rdf:resource="{}#{}"/>\n'.format(args.source_url, data))
                    elif "nif:nextWord" in token:
                        data = token.split()[1][1:]
                        outf.write('\t<nif:nextWord rdf:resource="{}#{}"/>\n'.format(args.source_url, data))
                    else:
                        meta = token.split()[0].replace("conll:", "")
                        data = token.split()[1][1:-1]

                        outf.write('\t<conll:{}>{}</conll:{}>\n'.format(meta, data, meta))

                outf.write('</rdf:Description>\n\n')

                word_counter += 1