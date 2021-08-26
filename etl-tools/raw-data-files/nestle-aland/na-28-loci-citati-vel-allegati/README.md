## What is this?

The loci citati vel allegati from NA 28. 

NOTE: The verses given here follow MT and not LXX. According to NA 28 foreword: 

"Chapter and verse numbers follow the usage of the Biblia Hebraica, even for quotations and allusions to the Septuagint18 (except for texts transmitted only in Greek). For books where the chapter and verse numbers of the Biblia Hebraica differ from those of the Septuagint or other versions, a table of these differences is given preceding the list of quotations." (p 87)

## Licensing/Copyright concerns

- Not going to post the raw data itself in Github, for copyright concerns. Instead, just manually ingest this csv separately.

## Use
```
python3 convert_na28_allegati_to_csv.py
```

That should be it.

## Steps to copy from the website and convert into CSV:
1) copy chart into txt files, one per book. Accordance doesn't allow for larger, and this actually might make it simpler to perform basic transformations on each file later
2) For each file, cut the title and versification differences between Hebrew and Greek out. Don't need these - other programs can do this translation for us automatically
3) run python script to convert these txt files into a good csv format, with proper book names and chapters and so on

### Some other manual changes I did to make it work
- Sometimes there is an extra "G" that sneaks in, marking a LXX entry. Just remove those, and make sure there is still a \t before the colon:
    * Esther 1:1
    * Daniel 3 has quite a few
- Note that I ignore the "etc" instances, which are two in NA 28 (there were more in NA 27), and they are fairly insignificant, so just ignoring
- Accordance has typo for Malachi 3:17, should be Eph 1:14 (as NA 27). 
- same for Exod 26:31, should be Mt 27,51. I reported both errors as of Aug 2021
- Psalm 145:17 has an extra character: </i>
- Ezek 1:1 for Matt 3:21 is wrong... Matthew 3 only has 17 verses

