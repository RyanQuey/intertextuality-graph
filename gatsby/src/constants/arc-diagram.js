import bookData from '../data/books';

// make these functions, so even if the option list changes, these will stay the same. Advantages of
// immutable stuff
export const initialChapterOption = () => ({label: 1, value: 1})
export const initialVerseOption = () => ({label: 1, value: 1})

export const bookOptions = bookData.map(b => ({
  label: b, 
  value: b}
))

// for now, just user data or TSK data
export const dataSetOptions = [
  // includes uploads and through the form
  {
    label: "All", 
    value: "all",
  },
  {
    label: "User Data", 
    value: "user",
  },
  {
    label: "Treasury of Scripture Knowledge", 
    value: "treasury-of-scripture-knowledge",
  },
]

export const allusionDirectionOptions = [
  {
    label: "Text alludes to", 
    value: "alludes-to",
  },
  {
    label: "Texts Alluded to By", 
    value: "alluded-to-by",
  },
  // {
  // // TODO might just make these "alluded-to" or something, not sure yet. Make sure to prefer precision over performance though, performance is fine
  //// for synoptic gospels, or James > Gospels, or Hebrews > John.
  //   label: "Shared Source", 
  //   value: "shared-source", 
  // },
  // {
  //   label: "All", 
  //   value: "all",
  // },
]

// allow between 1 and 4 hops
export const hopsCountOptions = [...Array(4).keys()].map(hopCount => ({
  label: hopCount + 1, 
  value: hopCount + 1}
))
