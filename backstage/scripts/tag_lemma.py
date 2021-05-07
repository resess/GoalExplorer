import argparse
import csv
import re

import numpy
import pandas
import webcolors
from gensim.models import Word2Vec
from nltk.corpus import stopwords, wordnet, words
from spacy.en import English


# data_dir = os.environ.get('SPACY_DATA', LOCAL_DATA_DIR)


class Filter(object):
    model_file = 'data/GoogleNews-vectors-negative300.bin'
    lorem = ['lorem', 'ipsum', 'dolor sit', 'dolor', 'amet', 'consectetur', 'adipiscing', 'elit']
    short_words = ['he', 'll', 've', 's', 't', 'a', 'the', 'to']
    new_words = ['offline', 'username']
    extra_words = {'sms': 'sms', 'log out': 'logout', 'sign in': 'login', 'log in': 'login',
                   'sign out': 'logout'}
    extra_stopwords = ['null', 'button', 'menu', 'aaa', 'abc', 'adc', 'adn', 'aed', 'ana', 'ans', 'apr', 'arr', 'att',
                       'aug', 'bak', 'bcc', 'biz', 'ble', 'bmi', 'bpm', 'ccc', 'cer', 'cgi', 'chg', 'chm', 'chs', 'cia',
                       'cie', 'clr', 'cls', 'col', 'com', 'con', 'cot', 'cry', 'csc', 'csv', 'ddd', 'def', 'dep', 'des',
                       'diy', 'dom', 'dsp', 'dvr', 'edu', 'ers', 'eta', 'exo', 'ext', 'foo', 'fry', 'fsa', 'ghi', 'gps',
                       'hai', 'haz', 'hoo', 'hoy', 'hue', 'hug', 'ice', 'imo', 'ink', 'ios', 'jkl', 'jog', 'joh', 'jpg',
                       'khz', 'kph', 'lng', 'los', 'lot', 'low', 'lpi', 'lun', 'maj', 'mar', 'mdx', 'meh', 'men', 'mes',
                       'mie', 'mlb', 'mms', 'mph', 'nah', 'nie', 'odi', 'otc', 'par', 'pas', 'pen', 'pmt', 'pol', 'por',
                       'rae', '\'re', 'rnd', 'rue', 'sab', 'sam', 'sat', 'sel', 'sen', 'sep', 'set', 'shp', 'sin',
                       'sit', 'sku', 'soh', 'sol', 'spa', 'src', 'sta', 'stg', 'str', 'sua', 'tak', 'tap', 'tau', 'thu',
                       'tos', 'tue', 'uce', 'use', 'vie', 'vin', 'vod', 'yds', 'ytd']
    non_stopwords = ["not", "ok", "yes", "no", "from", "go", "buy", "tv", "next", "back", "do", "like", "next", "close",
                     "backup", "about","skip"]
    allowed_tags = {'NN', 'NNS', 'NNP', 'NNPS', 'VB', 'VBD', 'VBN', 'VBP', 'VBZ', 'UH'}  # no gerund VBG
    rnorm = re.compile('[\n\r";]')

    def __init__(self):
        self.nlp = English()
        self.model = Word2Vec.load_word2vec_format(self.model_file, binary=True)

        self.new_words.extend(list(self.extra_words.values()))
        self.sw = set(stopwords.words('english'))
        self.sw.update(webcolors.CSS3_NAMES_TO_HEX)  # remove colors
        self.sw.update(self.lorem)
        self.sw.update(self.extra_stopwords)
        for w in self.non_stopwords:
            self.sw.discard(w)
        self.english_vocab = set(w.lower() for w in words.words('en'))
        self.english_vocab.update(wordnet.words('eng'))
        self.english_vocab.update(self.new_words)
        self.word_set = set(self.model.index2word)
        self.num_features = 300

        # self.tag_map = {'NN': 'n', 'VB': 'v'}

    def handle_context(self, text):
        text = re.sub('[\\?!&]', '.', text)
        phrases = text.split('.')
        res = set()
        for phrase in phrases:
            p = self.clean_label(phrase.strip())
            if p != '':
                res.add(p)
        return '.'.join(res), res

    def get_words(self, text):
        res = list()
        words = text.split()
        for word in words:
            if word in self.word_set and word not in self.sw:
                res.append(word)
        return res

    def make_feature_vec(self, words):
        words = self.get_words(words)
        featureVec = numpy.zeros((self.num_features,), dtype="float32")
        nwords = 0.
        for word in words:
            if word in self.word_set:
                nwords = nwords + 1.
                featureVec = numpy.add(featureVec, self.model[word])
        featureVec = numpy.divide(featureVec, nwords)
        return featureVec

    def process_csv(self, in_file, out_file, vec_file):
        with open(in_file, 'r') as csvfile, open(out_file, 'w') as csvout:
            labels = dict()
            cache = dict()
            reader = csv.DictReader(csvfile, delimiter=';')
            header = reader.fieldnames
            writer = csv.DictWriter(csvout, fieldnames=header, delimiter=';', quotechar='"', quoting=csv.QUOTE_ALL)
            writer.writeheader()
            idx = 0
            for row in reader:
                idx += 1
                if idx % 10000 == 0:
                    print(idx)
                try:
                    if '#' in row['label']:
                        continue  # skip double labels
                    if row['label'] in cache.keys():
                        label = cache[row['label']]
                    else:
                        label = self.clean_label(row['label'])
                        if label == '':
                            continue
                        cache[row['label']] = label
                        if label not in labels.keys():
                            labels[label] = self.make_feature_vec(label)

                    if row['context'] in cache.keys():
                        context = cache[row['context']]
                    else:
                        context, phrases_set = self.handle_context(row['context'])
                        if context == '':
                            continue  # skip empty context
                        cache[row['context']] = context
                        for phrase in phrases_set:
                            if phrase not in labels.keys():
                                labels[phrase] = self.make_feature_vec(phrase)
                    row['label'] = label
                    row['context'] = context
                    row['rawtext'] = self.norm(row['rawtext'])
                    writer.writerow(row)
                except:
                    raise
        print("saving vectors")
        X = numpy.array([l for l in labels.values()])
        print("saving...")
        df = pandas.DataFrame(X, index=labels.keys(), columns=range(self.num_features))
        df.to_csv(vec_file, sep=';')

    def norm(self, text):
        if text is not None:
            return self.rnorm.sub('', text)
        else:
            return ''

    def is_known_word(self, w):
        return (w not in self.sw) and (len(w) > 2 or w in self.non_stopwords) and (w in self.word_set)
        # return (w not in self.sw) and (w in self.english_vocab) and (len(w) > 1) and (w in self.word_set())

    def sub_extra(self, text):
        for key, val in self.extra_words.items():
            text = text.replace(key, val)
        return text

    def clean_label(self, text):
        text = self.sub_extra(text)
        text = text.replace('-', '').replace('#', ' ').replace('/', ' ').replace('\\', ' ')
        text = re.sub('[0-9]', '', text)
        # regex = re.compile('[%s]' % re.escape(string.punctuation))
        doc = self.nlp(text)
        res = []
        for w in doc:
            if w.tag_ in self.allowed_tags or w.lemma_ in self.non_stopwords:
                word = w.lemma_ if w.text.lower() not in self.extra_words.values() else w.text.lower()
                if self.is_known_word(word):
                    res.append(word)
        return ' '.join(res)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='filter')
    parser.add_argument('-i', dest='data', required=True)
    parser.add_argument('-o', dest='out')
    parser.add_argument('-v', dest='vec')
    args = parser.parse_args()
    # args = parser.parse_args(['-i', 'ui_test.txt', '-o', 'ui_testl.txt', '-v', 'ui_vec_test.txt'])
    filt = Filter()
    filt.process_csv(args.data, args.out, args.vec)

# text = "Free Caddie doesn't help"
m = Filter()
# it = m.nlp(text)
# print([(t.lemma_, t.tag_) for t in it])
print(m.clean_label("BUY NOW"))
