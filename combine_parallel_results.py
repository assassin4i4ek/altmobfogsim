import os


def main():
    results_path = './results_20'
    combined_results_path = results_path + '/nsgaii_results_all.csv'
    with open(combined_results_path, 'w') as combined_results_file:
        combined_results_file.write('numMobiles\tpopulationSize\tmapoModelMaxEvaluations\tinjectedSolutionsFraction\tavg\tstd\tstd_percent\ttime\n')
    for res_filename in os.listdir(results_path):
        if res_filename.endswith('.csv') and res_filename not in combined_results_path:
            print(res_filename)
            with open(os.path.join(results_path, res_filename), 'r') as partial_results_file, open(combined_results_path, 'a') as combined_results_file:
                combined_results_file.writelines(partial_results_file.readlines())


if __name__ == '__main__':
    main()
