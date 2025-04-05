-- KEYS[1]: lock key
-- ARGV[1]: lock value (thread id)
-- ARGV[2]: lock expiration (milliseconds)

-- Redis에 키가 없을 경우에만 설정
if redis.call('exists', KEYS[1]) == 0 then
    redis.call('set', KEYS[1], ARGV[1], 'PX', ARGV[2])
    return true
else
    return false
end