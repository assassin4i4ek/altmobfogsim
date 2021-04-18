import asyncio
import json
import sys
from functools import reduce


async def cleanup(subprocess_services):
    for subprocess in subprocess_services.values():
        subprocess.stdin.close()
        await subprocess.wait()


async def main(subprocess_services):
    num_processors, experiment_jar, config_path = sys.argv[1:]
    num_processors = int(num_processors)
    with open(config_path) as configFile:
        config = json.load(configFile)

    for slot_id in range(0, num_processors):
        subprocess_services[slot_id] = await asyncio.create_subprocess_exec(
            'C:/Users/Admin/.jdks/adopt-openjdk-14.0.2/bin/java.exe', '-jar', experiment_jar,
            f'results/nsgaii_results_{slot_id}.txt', f'results/nsgaii_results_{slot_id}.csv',
            stdout=open(f'results/out_{slot_id}.log', 'a'),
            stderr=sys.stdout,
            stdin=asyncio.subprocess.PIPE
        )

    slot_id = 0
    job_counts = {slot_id: 0 for slot_id in subprocess_services}
    total_jobs_per_service = reduce(lambda prod, x: x * prod,
                                    map(len, [config['numMobiles'],
                                              config['mapoModelMaxEvaluationsPerVariablePerPopulationSize'],
                                              config['populationSizePerNumVariables'],
                                              config['injectedSolutionsFractionPerNumVariables']])
                                    ) / len(subprocess_services)
    for num_mobiles in config['numMobiles']:
        for evals_per_var_per_pop_size in config['mapoModelMaxEvaluationsPerVariablePerPopulationSize']:
            for pop_size_per_var in config['populationSizePerNumVariables']:
                for inject_per_var in config['injectedSolutionsFractionPerNumVariables']:
                    pop_size = int(round(pop_size_per_var * num_mobiles * 3))
                    evals = int(round(evals_per_var_per_pop_size * pop_size * num_mobiles * 3))
                    injects = inject_per_var
                    params = list(map(str, (num_mobiles, pop_size, evals, injects)))
                    print(f'Submitting task {params} to {slot_id}')
                    params = (f'{"%.2f" % (100 * job_counts[slot_id] / total_jobs_per_service)}%',
                              f'results/nsgaii_results_{slot_id}.txt',
                              f'results/nsgaii_results_{slot_id}.csv',
                              *params)
                    # subprocess_services[slot_id].stdin.write(f'{" ".join(params)}\n'.encode())
                    job_counts[slot_id] += 1
                    slot_id = (slot_id + 1) % len(subprocess_services)

    print('finished submitting')


if __name__ == '__main__':
    loop = asyncio.ProactorEventLoop()
    asyncio.set_event_loop(loop)
    new_subprocess_services = {}
    try:
        loop.run_until_complete(main(new_subprocess_services))
    finally:
        loop.run_until_complete(cleanup(new_subprocess_services))
        loop.close()
