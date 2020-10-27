import argparse
import re


def adjuts_ttl_file(args):
    skip_lines = 1
    with open(args.input_path, "r", encoding="utf-8") as file_in:
        for line in file_in:
            if line.startswith("# "):
                skip_lines += 1
            else:
                if not line == "\n":
                    skip_lines = 0
                break

    add_prefix = True
    with open(args.input_path, "r", encoding="utf-8") as file_in, open(args.output_path, "w", encoding="utf-8") as file_out:
        file_out.write("# {}\n".format(args.comment1))
        file_out.write("# {}\n".format(args.comment2))
        file_out.write("# {}\n\n".format(args.comment3))

        for i, line in enumerate(file_in):
            if i < skip_lines:
                continue

            if line.startswith("# newdoc id"):
                for subline in line.split("# ", 3):
                    if subline != "":
                        file_out.write("# {}\n".format(subline.replace("\n", "")))
                continue

            if line.startswith("# sent_id"):
                for subline in line.split("# ", 2):
                    if subline != "":
                        file_out.write("# {}\n".format(subline.replace("\n", "")))
                continue

            line = re.sub(r":(s\d+_\d+)", r":rrt_\1", line)

            if not line.startswith("@prefix") or add_prefix:
                file_out.write(line)

            if line == "\n":
                add_prefix = False


def adjust_xml_file(args):
    skip_lines = 1
    with open(args.input_path, "r", encoding="utf-8") as file_in:
        for line in file_in:
            if line.startswith("<!-- "):
                skip_lines += 1
            else:
                if not line == "\n":
                    skip_lines = 0
                break

    with open(args.input_path, "r", encoding="utf-8") as file_in, open(args.output_path, "w", encoding="utf-8") as file_out:
        file_out.write("<!-- {} -->\n".format(args.comment1))
        file_out.write("<!-- {} -->\n".format(args.comment2))
        file_out.write("<!-- {} -->\n\n".format(args.comment3))

        for i, line in enumerate(file_in):
            if i < skip_lines:
                continue

            line = re.sub(r"#(s\d+_\d+)", r"#rrt_\1", line)

            file_out.write(line)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_path")
    parser.add_argument("output_path")
    parser.add_argument("--type", default="ttl")
    parser.add_argument("--comment1", default="The Romanian UD treebank (called RoRefTrees) (Barbu Mititelu et al., "
                                              "2016) is the reference treebank in UD format for standard Romanian.")
    parser.add_argument("--comment2", default="If using this corpus, please cite: V. Barbu Mititelu, R. Ion, R. "
                                              "Simionescu, E. Irimia, C-A Perez. 2016. The Romanian Treebank Annotated"
                                              " According to Universal Dependencies. Proceedings of HrTAL2016.")
    parser.add_argument("--comment3", default="More Romanian LLOD resources: http://www.racai.ro/p/llod/")
    parser.add_argument("--prefix", default="rrt_")

    args = parser.parse_args()

    if args.type == "ttl":
        adjuts_ttl_file(args)
    elif args.type == "xml":
        adjust_xml_file(args)
    else:
        ValueError("The type argument '{}' is not supported".format(args.type))

