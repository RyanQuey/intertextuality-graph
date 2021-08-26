import re
import os
import csv

# following the convention used in the my csv bulk uploader
headers = ("source-starting-book", "source-starting-chapter", "source-starting-verse", "source-ending-book", "source-ending-chapter", "source-ending-verse", "alluding-starting-book", "alluding-starting-chapter", "alluding-starting-verse", "alluding-ending-book", "alluding-ending-chapter", "alluding-ending-verse", "confidence-level", "volume-level", "comments")

# http://adfontespress.com/matthewjbarron/books-of-the-bible-abbreviated-in-english-and-german/
book_name_mapping = {
    "Mt": "Matthew",
    "Mc": "Mark",
    "L": "Luke",
    "J": "John",
    "Act": "Acts of the Apostles",
    "R": "Romans",
    "1K": "1 Corinthians",
    "2K": "2 Corinthians",
    "G": "Galatians",
    "E": "Ephesians",
    "Ph": "Philippians",
    "Kol": "Colossians",
    "1Th": "1 Thessalonians",
    "2Th": "2 Thessalonians",
    "1T": "1 Timothy",
    "2T": "2 Timothy",
    "Tt": "Titus",
    "Phm": "Philemon",
    "H": "Hebrews",
    "Jc": "James",
    "1P": "1 Peter",
    "2P": "2 Peter",
    "1J": "1 John",
    "2J": "2 John",
    "3J": "3 John",
    "Jd": "Jude",
    "Ap": "Revelation",
}

def parse_comma_separated_ref(ref):
    """
    Examples of complex entries:
    - 71,2.15s.18 (for OT side, in Psalms.txt)
    - 9a (for OT side, in Job.txt)
        - which in this case, means additional parts of a verse in the LXX but not in MT I think? . Either way, we can ignore...I'm pretty sure. In this example, there are parts a-e, so let's get rid of all
    - 7,3.11.17.21 (for NT side in Psalms.txt)

    NOTE: Different behavior for NT and OT refs will be implemented here. 
    - while for NT refs we will treat discrete versus as separate entities, for OT refs we will combine these discrete references as a single range. This logic will be implemented in the code elsewhere though, not here. Keep this method agnostic of that behavior. 
    """

    split_ref = ref.split(",")

    # handle for books where there is only a verse, no chapter (e.g., 2 John). 
    if len(split_ref) == 1:
        new_chapter = 1
        verse_entry = split_ref[0]

    else:
        # I believe only one chapter will ever be given here according to NA 28 conventions. Need to confirm though TODO
        new_chapter = split_ref[0]

        # verse entry can include multiple verses
        verse_entry = split_ref[1]

    # Next: parse the verse entry into verse ranges
    verse_ranges = parse_verse_entry(verse_entry)

    return {
        "chapter": new_chapter,
        "verse-ranges": verse_ranges
    }

def parse_verse_entry(verse_entry):
    """
    takes verse entry (e.g., "21s" or "45.47") and parse it, returning verse ranges (a list, with comma separated rather than "s" or "." there. Should be no more commas after this either)
    - watch out for these: "9a" (for OT side, in Job.txt)
        - which in this case, means additional parts of a verse in the LXX but not in MT I think? . Either way, we can ignore...I'm pretty sure. In this example, there are parts a-e, so let's get rid of all
    - there are two examples of "etc" remaining, Josh 1:2 (from Heb 3:5) and 1 Kings 13:1 (from 1 Tim 6:11). There were more in NA 27 but most were removed in NA 28. Let's just ignore these.
    """

    # Next: parse the verse entry
    # - hyphens means verse range; period means discrete verse; s means "f", one following verse.
    # - multiple periods are allowed in a single entry. 
    # - only single hyphens or s allowed. 

    # first, split by periods
    verse_ranges = verse_entry.split(".")
    # then, convert any "s" annotations to a hyphen notation
    # e.g,. "7s" -> "7-8" (this would be verses 7-8 of the specified chapter)
    def s_to_hypen(verse_range):
        # first actually going to clean up the extra letters first
        verse_range = verse_range.replace("a", "")
        verse_range = verse_range.replace("b", "")
        verse_range = verse_range.replace("c", "")
        verse_range = verse_range.replace("d", "")
        verse_range = verse_range.replace("e", "")

        # this to get rid of the last letter of any "etc" instances (see note above in comment for this method). e and c are taken care of above. 
        verse_range = verse_range.replace("t", "")

        if "s" in verse_range:
            starting_v_str = verse_range.replace("s", "")
            ending_v = int(starting_v_str) +1
            return f"{starting_v_str}-{ending_v}"
        else:
            return verse_range


    parsed_verse_ranges = map(s_to_hypen, verse_ranges)

    return parsed_verse_ranges


def crazy_alluding_refs_str_splitter(txt, seps):
    """

    - for splitting crazy alluding refs such as "Mt 22,44; 26,64 Mc 12,36; 14,62; 16,19 L 20,42; 22,69 Act 2,34 R 8,34 1K 15,25 E 1,20 H 1,3.13; 8,1; 10,12" into separate sections.
    - Source: https://stackoverflow.com/a/4697047/6952495
    - one difference: always remove the first item, since we will always have a book name first, so the first item will be a blank string

    @param seps - list of separators to use
    @param txt  - the text to split
    """
    default_sep = seps[0]

    # we skip seps[0] because that's the default separator
    for sep in seps[1:]:
        txt = txt.replace(sep, default_sep)

    split_items = [i.strip() for i in txt.split(default_sep)]
    # always remove the first item, since we will always have a book name first, so the first item will be a blank string

    return split_items[1:]

def verse_range_to_verse_list(verse_ranges):
    """
    takes a list of verse ranges (e.g., ["7", "8-9", "11-13"]) and returns list of individual numbers [7,8,9,11,12,13]
    - "s" in verse numbers should be gone already by this point
    """
    final = []
    for vrange in verse_ranges:
        # https://stackoverflow.com/a/55851158/6952495
        list_for_this_range = range(*[int(n) for n in vrange.split("-")])

        # add to final list
        final.extend(list_for_this_range)

    return final

csv_filename = "ot-use-in-nt.na-28.csv"
current_dir_path = os.path.dirname(os.path.realpath(__file__))





def main():
    """
    takes the crazy NA-28 format and converts to a single nice CSV.
    - assumes you followed instructions in the README in this folder to prepare the txt files.
    - headers: following the convention used in the my csv bulk uploader, for greatest flexibility in verse numbers received
    - want to test out? Use this in python3 cli: 
        import convert_na28_allegati_to_csv
    """

    # write header for csv
    with open(csv_filename, "w") as csv_file:
        writer = csv.DictWriter(csv_file, delimiter="|", fieldnames=headers)
        writer.writeheader()


    for filename_str in os.listdir(current_dir_path):
        # NOTE skips if not a .txt file, even after we get here
        parseFileForBook = ParseFileForBook(filename_str)
        parseFileForBook.append_rows_for_file()

class ParseFileForBook():
    book = None
    current_source_chapter = None

    def __init__(self, filename_str):
        self.filename = filename_str
        self.book = str(self.filename).replace(".txt", "")


    def append_rows_for_file(self):
        """
        for a given filename, iterate over rows in the txt file and append rows to our master csv file
        """
        filename_obj = os.fsdecode(self.filename)

        if filename_obj.endswith(".txt"):
            print("\n\n== parsing file:", self.filename)
        else:
            print("\n\nskipping file:", self.filename)
            return

        # read file and get lines

        with open(self.filename, 'r') as file:
            for entry_row in file.read().splitlines():
                entry_data = self.parse_single_entry(entry_row)

                # write to csv
                self.append_entry_data(entry_data)

    def append_entry_data(self, entry_row):
        """
        takes dict of data and appends to the csv file
        """
        with open(csv_filename, "a") as csv_file:
            writer = csv.DictWriter(csv_file, delimiter="|", fieldnames=headers)
            dict_for_ref_base = dict(entry_row)
            del dict_for_ref_base["alluding-references"]

            for alluding_ref in entry_row["alluding-references"]:

                # merge the base with the values for this alluding ref
                dict_for_ref = {**dict_for_ref_base, **alluding_ref}

                writer.writerow(dict_for_ref)
            

    def parse_single_entry(self, entry_row):
        """
        - expects row to be in format like this:
            14,2-32 : Mt 8,4 Mc 1,44 L 5,14

        ie, <OT chapter/verses>\t: <nt refs>

        @return entry_data dict
            {
                "source-starting-book": ,
                "source-ending-book": ,
                "source-starting-chapter": ,
                "source-ending-chapter" 
                "source-starting-verse": ,
                "source-ending-verse" ,
                "alluding-references": [
                    {
                        "alluding-starting-book": ,
                        "alluding-ending-book": ,
                        "alluding-starting-chapter": ,
                        "alluding-ending-chapter" 
                        "alluding-starting-verse": ,
                        "alluding-ending-verse" ,
                    },
                    ...
                ]
            }
        """
        #########################
        # first split data entry row into OT reference then NT reference
        print("entry_row", entry_row)
        split_row = entry_row.split("\t: ")

        print("split_row", split_row)
        ot_ref = split_row[0]
        nt_refs_str = split_row[1]

        entry_data = {
            "source-starting-book": self.book,
            "source-ending-book": self.book,
        }

        ot_ref_data = self.parse_single_ot_ref(ot_ref)

        # next, iterate over each NT ref, and parse out the ref info.
        alluding_references = self.parse_nt_refs_for_entry(nt_refs_str)

        # merge in ot_ref_data and nt references
        entry_data = {**entry_data, **ot_ref_data}
        entry_data["alluding-references"] = alluding_references

        return entry_data



    def parse_nt_refs_for_entry(self, nt_refs_str):
        """
        takes a string containing one or more nt references, converts to list, then iterates over
        @param nt_refs_str - can look as bad as this: 
            * Mt 3,17 Mc 1,11 L 3,22 J 1,34 Act 13,33 H 1,5; 5,5 (from Psalms.txt)
            * "Mt 22,44; 26,64 Mc 12,36; 14,62; 16,19 L 20,42; 22,69 Act 2,34 R 8,34 1K 15,25 E 1,20 H 1,3.13; 8,1; 10,12"

        @return list alluding_references - looks like this
            [
                {
                    "alluding-starting-book": ,
                    "alluding-ending-book": ,
                    "alluding-starting-chapter": ,
                    "alluding-ending-chapter" 
                    "alluding-starting-verse": ,
                    "alluding-ending-verse" ,
                },
                ...
            ]
        """
        alluding_references = []
        # split into different books. E.g., ["Mt 3,17", "Mc 1,11", "L 3,22", "J 1,34", "Act 13,33", "H 1,5; 5,5"]

        # idea is that each book is first identified by maybe a number (e.g., with 1Th) then a capital letter, then maybew some lowercase letters, and then after that it's all numbers and punctuation EXCEPT maybe the letter s (Eng normally uses "f", it's for one subsequent verse).
        # - the fact that it can have lower case 's' in the verse part is why we need to make sure to specify the uppercase part in the book name
        # - also have to make sure NOT to include the first integer of a following book that starts with integer ("1K" for "1 Cor" for example) in the ch/v part, hence the {2,28} part, 28 being just way bigger than the biggest clump. But excludes anything smaller than length of 2 from the ch/v part. 

        # since this is difficult, I'm cheating. First get all the books
        books_of_nt_refs_by_book = re.findall(r'[0-9]?[A-Z][a-z]*', nt_refs_str)
        # now books_of_nt_refs_by_book is something like ['Mt', 'Mc', 'L', 'Act', 'R', '1K', 'E', 'H']. 

        # take that and split all the refs with it
        nt_refs_for_book = crazy_alluding_refs_str_splitter(nt_refs_str, books_of_nt_refs_by_book)
        # nt_refs_for_book should be like: 
        #   ['22,44; 26,64', '12,36; 14,62; 16,19', '20,42; 22,69', '2,34', '8,34', '15,25', '1,20', '1,3.13; 8,1; 10,12']

        # since the nt_refs_for_book is in order, we can now merge it back in with its book. 
        # Iterate over the books, and match the refs for that book by index
        for i, book in enumerate(books_of_nt_refs_by_book):
            # since the nt_refs_for_book is in order, we can now merge it back in with its book. 
            matching_ch_and_v_refs_str = nt_refs_for_book[i]

            # split into separate refs by semicolon
            # result should be e.g., for Hebrews, ['1,3.13', '8,1', '10,12']
            matching_ch_and_v_refs  = matching_ch_and_v_refs_str.split("; ")

            # iterate over these refs
            for matching_ref in matching_ch_and_v_refs:
                # then split once more, this time by periods. Using our helper for this:
                parsed = parse_comma_separated_ref(matching_ref) 
                chapter = parsed["chapter"]
                verse_ranges = parsed["verse-ranges"]

                for verse_range in verse_ranges:
                    # iterate over these refs, and append each parsed ref to our main list
                    entry = {
                        "alluding-starting-book": book_name_mapping[book],
                        "alluding-ending-book": book_name_mapping[book],
                        "alluding-starting-chapter": chapter,
                        "alluding-ending-chapter": chapter,
                        "alluding-starting-verse": verse_range[0],
                        "alluding-ending-verse": verse_range[-1],
                    }

                    # append each entry to final list
                    alluding_references.append(entry) 

        return alluding_references

    def parse_single_ot_ref(self, ot_ref):
        """
        # - chapters are marked only once for each chapter...I guess they were trying to save ink. Can be marked in at least a couple different ways:
        #   * c.<chapter-number>
        #   * <chapter-number>,<verse-number[s]>

        NOTE!! - while for NT refs we will treat discrete versus as separate entities, for OT refs we will combine these discrete references as a single range.

        @return ref_data dict - represents a single reference to an OT passage
        """

        ## Checking for chapter marker
        # check for "c.<chapter-number>" syntax
        new_chapter = False
        ref_data = {}

        if "c." in ot_ref:
            new_chapter = ot_ref.replace("c.", "")
            # no verses needed

        elif "," in ot_ref:
            parsed = parse_comma_separated_ref(ot_ref)
            new_chapter = parsed["chapter"]
            verse_ranges = parsed["verse-ranges"]
            # combine into single range for OT ref, since the way that NA 28 appendix is built means that this single OT ref is being referred to by one or more NT refs. This means that the OT refs are assumed to be a single passage. 
            # not assuming verses are still in order, just to be safe
            all_verses_in_ranges = verse_range_to_verse_list(verse_ranges)
            ref_data["source-starting-verse"] = min(all_verses_in_ranges)
            ref_data["source-ending-verse"] = max(all_verses_in_ranges)
        else:
            # left side is just a verse string. Don't set the chapter, but do set the verse
            # in this case, the ot_ref is a verse_entry
            verse_ranges = parse_verse_entry(ot_ref)

            # combine into single range for OT ref, since the way that NA 28 appendix is built means that this single OT ref is being referred to by one or more NT refs. This means that the OT refs are assumed to be a single passage. 
            # not assuming verses are still in order, just to be safe
            all_verses_in_ranges = verse_range_to_verse_list(verse_ranges)
            ref_data["source-starting-verse"] = min(all_verses_in_ranges)
            ref_data["source-ending-verse"] = max(all_verses_in_ranges)


        ref_data["source-starting-chapter"] = new_chapter if new_chapter else self.current_source_chapter

        # only does one chapter at a time
        ref_data["source-ending-chapter"] = ref_data["source-starting-chapter"]
        self.current_source_chapter = ref_data["source-starting-chapter"]

        return ref_data

if __name__ == '__main__':
    # execute only if run as the entry point into the program
    main()
    print("\n\n -- done.")


